package com.hse.reporting.projectmanagement;

/**
 * EC-36 — a project with the same name already exists; the message names the
 * duplicated project (PRD EC-36: "message says the name").
 */
public class DuplicateProjectNameException extends ProjectValidationException {

    private final String duplicateName;

    public DuplicateProjectNameException(String duplicateName) {
        super("A project named \"" + duplicateName + "\" already exists. Choose a different name.");
        this.duplicateName = duplicateName;
    }

    public String getDuplicateName() {
        return duplicateName;
    }
}
