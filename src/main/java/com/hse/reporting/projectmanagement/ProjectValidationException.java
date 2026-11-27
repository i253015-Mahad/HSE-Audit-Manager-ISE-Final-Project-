package com.hse.reporting.projectmanagement;

/**
 * Project-level validation failure (blank name, operation on a closed project,
 * etc.). The message is safe to show to the user.
 */
public class ProjectValidationException extends RuntimeException {

    public ProjectValidationException(String message) {
        super(message);
    }
}
