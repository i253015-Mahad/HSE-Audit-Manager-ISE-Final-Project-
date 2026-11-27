package com.hse.reporting.useraccess.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the public error pages referenced by SecurityConfig:
 * <ul>
 *   <li>{@code /session-expired} — FR-08: an expired session is reported with an
 *       explanation instead of a silent redirect (reached via invalidSessionUrl).</li>
 *   <li>{@code /403} — shown to a signed-in user who lacks the required role
 *       (accessDeniedPage); the interesting reason comes from
 *       {@link AccessDeniedAdvice} when a use case denied the operation.</li>
 * </ul>
 */
@Controller
public class ErrorPageController {

    @GetMapping("/session-expired")
    public String sessionExpired() {
        return "error/session-expired";
    }

    @GetMapping("/403")
    public String accessDenied() {
        return "error/403";
    }
}
