package com.hse.reporting.useraccess.web;

import com.hse.reporting.useraccess.access.AuthorizationDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * Translates authorization denials raised by use cases or the actor resolution
 * into HTTP 403 with the dedicated page. This keeps enforcement inside the
 * application layer while the presentation only maps the outcome to a response.
 */
@ControllerAdvice
public class AccessDeniedAdvice {

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ModelAndView authorizationDenied(AuthorizationDeniedException ex) {
        ModelAndView view = new ModelAndView("error/403", HttpStatus.FORBIDDEN);
        view.addObject("reason", ex.getMessage());
        return view;
    }
}
