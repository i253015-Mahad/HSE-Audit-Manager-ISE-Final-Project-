package com.hse.reporting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hse.reporting.useraccess.access.Actor;
import com.hse.reporting.useraccess.access.AuthorizationDeniedException;
import com.hse.reporting.useraccess.domain.Role;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class ActorContextTest {

    private final ActorContext actorContext = new ActorContext();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void identifiesTheAuthenticatedActorWithEmailAndRole() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "mahadwaqas28@gmail.com", "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_MANAGER"))));

        Actor actor = actorContext.currentActor();

        assertThat(actor.email()).isEqualTo("mahadwaqas28@gmail.com");
        assertThat(actor.role()).isEqualTo(Role.MANAGER);
    }

    @Test
    void anonymousRequestHasNoActor() {
        SecurityContextHolder.clearContext();
        assertThat(actorContext.currentActorOrNull()).isNull();
        assertThatThrownBy(actorContext::currentActor)
                .isInstanceOf(AuthorizationDeniedException.class);
    }

    @Test
    void authenticatedUserWithoutSystemRoleIsRejected() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "someone@example.com", "n/a",
                        List.of(new SimpleGrantedAuthority("SOME_OTHER_AUTHORITY"))));

        assertThatThrownBy(actorContext::currentActor)
                .isInstanceOf(AuthorizationDeniedException.class);
    }
}
