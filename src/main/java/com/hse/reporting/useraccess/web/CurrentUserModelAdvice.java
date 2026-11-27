package com.hse.reporting.useraccess.web;

import com.hse.reporting.application.ActorContext;
import com.hse.reporting.useraccess.access.Actor;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Makes the current user's identity available to every template for layout
 * purposes (name, role-specific navigation). It renders what the user may see;
 * it never grants access — that is decided server-side by the use cases and the
 * URL rules (ASR-01).
 *
 * <p>Also carries the FR-08 expiry notice decision: a stale session id can mean
 * either a genuine idle timeout or a server restart, and Spring Security cannot
 * tell them apart. The {@code hse_last_seen} marker (refreshed while signed in)
 * distinguishes them — the notice is shown only when the user was actually
 * away longer than the session timeout.
 */
@ControllerAdvice
public class CurrentUserModelAdvice {

    /** Last time the user was seen signed in; used to detect a real FR-08 timeout. */
    private static final String LAST_SEEN_COOKIE = "hse_last_seen";

    private final ActorContext actorContext;
    private final Duration sessionTimeout;

    public CurrentUserModelAdvice(ActorContext actorContext,
                                  @Value("${server.servlet.session.timeout:30m}") Duration sessionTimeout) {
        this.actorContext = actorContext;
        this.sessionTimeout = sessionTimeout;
    }

    @ModelAttribute("currentUserEmail")
    public String currentUserEmail() {
        Actor actor = actorContext.currentActorOrNull();
        return actor == null ? "" : actor.email();
    }

    @ModelAttribute("currentUserRole")
    public String currentUserRole() {
        Actor actor = actorContext.currentActorOrNull();
        return actor == null ? "" : actor.role().name();
    }

    /** FR-08: report a real timeout on the login page; a restart is not an expiry. */
    @ModelAttribute("showSessionExpired")
    public boolean showSessionExpired(HttpServletRequest request, HttpServletResponse response) {
        Actor actor = actorContext.currentActorOrNull();
        if (actor != null) {
            Cookie marker = new Cookie(LAST_SEEN_COOKIE, Long.toString(Instant.now().toEpochMilli()));
            marker.setPath("/");
            marker.setHttpOnly(true);
            marker.setMaxAge(60 * 60 * 24 * 30);
            response.addCookie(marker);
            return false;
        }
        if (!expiredRequested(request)) {
            return false;
        }
        String lastSeen = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (LAST_SEEN_COOKIE.equals(cookie.getName())) {
                    lastSeen = cookie.getValue();
                }
            }
        }
        if (lastSeen == null) {
            return true; // no marker: report the expiry rather than hide it (PRD-safe)
        }
        try {
            return Duration.between(Instant.ofEpochMilli(Long.parseLong(lastSeen)), Instant.now())
                    .compareTo(sessionTimeout) >= 0;
        } catch (NumberFormatException badMarker) {
            return true;
        }
    }

    /** {@code invalidSessionUrl("/login?expired")} arrives as a raw query flag with no value. */
    private static boolean expiredRequested(HttpServletRequest request) {
        String query = request.getQueryString();
        if (query == null) {
            return false;
        }
        for (String pair : query.split("&")) {
            if ("expired".equals(pair.split("=", 2)[0])) {
                return true;
            }
        }
        return false;
    }
}
