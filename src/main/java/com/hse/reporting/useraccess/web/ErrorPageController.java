package com.hse.reporting.useraccess.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the public error page referenced by SecurityConfig:
 * <ul>
 *   <li>{@code /403} — shown to a signed-in user who lacks the required role
 *       (accessDeniedPage); the interesting reason comes from
 *       {@link AccessDeniedAdvice} when a use case denied the operation.</li>
 * </ul>
 *
 * <p>The FR-08 expired-session notice is no longer a separate page: it is shown
 * on the login page (see {@link CurrentUserModelAdvice}).
 */
@Controller
public class ErrorPageController {

    @GetMapping("/403")
    public String accessDenied() {
        return "error/403";
    }
}
