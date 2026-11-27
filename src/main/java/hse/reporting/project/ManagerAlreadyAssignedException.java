package hse.reporting.project;

/**
 * EC-10 — a project may have only ONE Manager at a time; adding a second
 * Manager while one exists is refused (PRD FR-31, EC-10, BR-15). Use
 * reassignment instead, which replaces the previous Manager.
 */
public class ManagerAlreadyAssignedException extends ProjectValidationException {

    public ManagerAlreadyAssignedException(String projectName, String currentManagerEmail) {
        super("Project \"" + projectName + "\" already has a Manager ("
                + currentManagerEmail + "). A second Manager cannot be added at the same time — "
                + "use reassign to replace the current Manager.");
    }
}
