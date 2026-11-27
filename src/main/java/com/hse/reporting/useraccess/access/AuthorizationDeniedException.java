package com.hse.reporting.useraccess.access;

/**
 * Thrown when the acting user is not permitted to perform the requested operation.
 * The presentation layer translates it into HTTP 403.
 */
public class AuthorizationDeniedException extends RuntimeException {

    public AuthorizationDeniedException(String message) {
        super(message);
    }
}
