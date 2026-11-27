package com.hse.reporting.application;

import com.hse.reporting.useraccess.access.Actor;
import com.hse.reporting.useraccess.access.AuthorizationDeniedException;
import com.hse.reporting.useraccess.domain.Role;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Application-layer service that identifies the acting user of the current
 * request (architecture.md section 5.2: "Identify the acting user and invoke the
 * appropriate authorization checks").
 *
 * <p>Use cases call {@link #currentActor()} before asking
 * {@code AuthorizationService} for a decision; presentation code only asks for
 * the actor to display identity, never to grant access by itself.
 */
@Component
public class ActorContext {

    /**
     * @return the authenticated acting user
     * @throws AuthorizationDeniedException if the request carries no authenticated user
     */
    public Actor currentActor() {
        Actor actor = currentActorOrNull();
        if (actor == null) {
            throw new AuthorizationDeniedException("An authenticated user is required");
        }
        return actor;
    }

    /**
     * @return the authenticated acting user, or {@code null} when anonymous
     *         (used by presentation code for layout decisions)
     */
    public Actor currentActorOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        Role role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(Role::fromAuthority)
                .filter(roleOrNull -> roleOrNull != null)
                .findFirst()
                .orElseThrow(() -> new AuthorizationDeniedException(
                        "No system role is assigned to " + authentication.getName()));
        return new Actor(authentication.getName(), role);
    }
}
