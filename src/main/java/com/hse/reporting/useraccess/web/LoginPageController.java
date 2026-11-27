package com.hse.reporting.useraccess.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Serves the sign-in page. The login form itself is processed by Spring Security. */
@Controller
public class LoginPageController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
