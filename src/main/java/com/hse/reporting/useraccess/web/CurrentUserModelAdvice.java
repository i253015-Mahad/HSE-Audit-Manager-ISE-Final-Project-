package com.hse.reporting.useraccess.web;

import com.hse.reporting.application.ActorContext;
import com.hse.reporting.useraccess.access.Actor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Makes the current user's identity available to every template for layout
 * purposes (name, role-specific navigation). It renders what the user may see;
 * it never grants access — that is decided server-side by the use cases and the
 * URL rules (ASR-01).
 */
@ControllerAdvice
public class CurrentUserModelAdvice {

    private final ActorContext actorContext;

    public CurrentUserModelAdvice(ActorContext actorContext) {
        this.actorContext = actorContext;
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
}
