package com.hse.reporting.projectmanagement;

/** Thrown when a project lookup by id fails (no such project). */
public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(Long id) {
        super("Project #" + id + " does not exist.");
    }
}
