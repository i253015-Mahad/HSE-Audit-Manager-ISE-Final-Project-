package com.hse.reporting.useraccess.web;

import com.hse.reporting.application.ActorContext;
import com.hse.reporting.useraccess.access.Actor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Root landing: sends the signed-in user to the entry page of their role
 * (FR-40). Navigation only — the access decision for each page is made by the
 * page's use case, so a forged redirect cannot bypass authorization.
 */
@Controller
public class HomeController {

    private final ActorContext actorContext;

    public HomeController(ActorContext actorContext) {
        this.actorContext = actorContext;
    }

    @GetMapping("/")
    public String home() {
        Actor actor = actorContext.currentActor();
        return switch (actor.role()) {
            case ADMIN -> "redirect:/admin";
            case MANAGER -> "redirect:/manager";
            case INSPECTOR -> "redirect:/inspector";
        };
    }
}
