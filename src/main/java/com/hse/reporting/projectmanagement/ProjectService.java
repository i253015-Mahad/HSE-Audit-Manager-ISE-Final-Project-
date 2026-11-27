package com.hse.reporting.projectmanagement;

import com.hse.reporting.useraccess.access.ManagerDirectory;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Project Management use cases (architecture §5.3.B; PRD FR-30…FR-36).
 *
 * <p>Owns project lifecycle: create (unique name), assign/reassign exactly one
 * Manager while open, and the confirmed closure sequence of flow 9.4 —
 * auto-close all Open reports, discard all Pending reports, then close the
 * project and stamp the retention clock (NFR-03).
 *
 * <p>Authorization (Admin-only) is enforced server-side: SecurityConfig guards
 * {@code /projects/**} and {@code ProjectController.requireAdmin()} re-checks
 * each handler (AGENTS.md §8, ASR-01).
 */
@Service
public class ProjectService {

    private final ProjectStore projects;

    /**
     * Port to the Safety Report Management module (Eman). Injected as
     * {@link ObjectProvider} so the application still runs before her bean
     * exists — the interim {@link ProjectReportSummary#DEFAULT} returns 0 /
     * no-op with loud TODO(EMAN) markers.
     */
    private final ObjectProvider<ProjectReportSummary> reportSummary;

    /** Fixed port for unit tests; {@code null} in production (uses the provider). */
    private final ProjectReportSummary fixedReportSummary;

    /** Company account directory — a Manager email must be a MANAGER account (BR-19). */
    private final ManagerDirectory managerDirectory;

    @Autowired
    public ProjectService(ProjectStore projects,
                          ObjectProvider<ProjectReportSummary> reportSummary,
                          ManagerDirectory managerDirectory) {
        this.projects = projects;
        this.reportSummary = reportSummary;
        this.fixedReportSummary = null;
        this.managerDirectory = managerDirectory;
    }

    /** Test seam: fixed port implementations directly. */
    ProjectService(ProjectStore projects, ProjectReportSummary reportSummary,
                   ManagerDirectory managerDirectory) {
        this.projects = projects;
        this.reportSummary = null;
        this.fixedReportSummary = reportSummary;
        this.managerDirectory = managerDirectory;
    }

    private ProjectReportSummary reports() {
        if (fixedReportSummary != null) {
            return fixedReportSummary;
        }
        ProjectReportSummary port = reportSummary != null ? reportSummary.getIfAvailable() : null;
        return port != null ? port : ProjectReportSummary.DEFAULT;
    }

    // ------------------------------------------------------------------ FR-30 / FR-31

    /**
     * FR-30 — Admin creates a project. Names must be unique (FR-31, EC-36).
     * The Manager email is added afterwards (FR-31), so it starts null.
     */
    public Project create(String name) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) {
            throw new ProjectValidationException("Project name is required.");
        }
        if (projects.existsByName(trimmed)) {
            throw new DuplicateProjectNameException(trimmed);
        }
        Project project = new Project(trimmed);
        return projects.save(project);
    }

    /**
     * FR-31 (first assignment) — attach the first Manager email. Refuses a
     * second simultaneous Manager (EC-10) and any change on a closed project
     * (FR-36).
     */
    public Project assignManager(Long projectId, String managerEmail) {
        Project project = requireOpen(projectId);
        if (project.getManagerEmail() != null && !project.getManagerEmail().isBlank()) {
            throw new ManagerAlreadyAssignedException(project.getName(), project.getManagerEmail());
        }
        project.setManagerEmail(requireEmail(managerEmail, project));
        return projects.save(project);
    }

    /**
     * FR-31 / FR-45 (reassignment, while open) — replace the Manager email.
     * The new Manager gains pending approvals and existing reports; the
     * previous Manager loses them (visibility is query-scoped — Eman's side).
     * Refused when no Manager is set (use assign) or when closed (FR-36).
     */
    public Project reassignManager(Long projectId, String newManagerEmail) {
        Project project = requireOpen(projectId);
        if (project.getManagerEmail() == null || project.getManagerEmail().isBlank()) {
            throw new ProjectValidationException(
                    "Project \"" + project.getName() + "\" has no Manager yet — assign one instead.");
        }
        String trimmed = newManagerEmail == null ? "" : newManagerEmail.trim();
        if (trimmed.equalsIgnoreCase(project.getManagerEmail())) {
            throw new ProjectValidationException("\"" + trimmed + "\" is already the Manager of \""
                    + project.getName() + "\" — enter a different Manager's email to reassign.");
        }
        project.setManagerEmail(requireEmail(trimmed, project));
        return projects.save(project);
    }

    // ------------------------------------------------------------------ FR-32…FR-35 (flow 9.4)

    /** Warning inputs for the closure confirmation page (FR-32 / FR-35). */
    public long countOpenReports(Long projectId) {
        return reports().countOpen(projectId);
    }

    public long countPendingReports(Long projectId) {
        return reports().countPending(projectId);
    }

    /**
     * FR-33 / FR-35 — the CONFIRMED closure sequence: auto-close all Open
     * reports, discard all Pending Approval reports, then close the project
     * and stamp {@code closedAt} (exact timestamp starts the 3-month
     * retention window, NFR-03). Declining the confirmation never reaches
     * this method (FR-34 — controller simply does nothing).
     */
    public Project closeConfirmed(Long projectId) {
        Project project = requireOpen(projectId);
        reports().closeAllOpen(projectId);   // FR-33
        reports().discardPending(projectId); // FR-35 (pending discarded on confirm)
        project.setState(Project.State.CLOSED);
        project.setClosedAt(LocalDateTime.now()); // NFR-03 retention start
        return projects.save(project);
    }

    // ------------------------------------------------------------------ queries

    public List<Project> findAll() {
        return projects.findAll();
    }

    public Project get(Long projectId) {
        return projects.find(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }

    // ------------------------------------------------------------------ internals

    private Project requireOpen(Long projectId) {
        Project project = get(projectId);
        if (!project.isOpen()) {
            throw new ProjectValidationException(
                    "Project \"" + project.getName() + "\" is closed and can no longer be changed (FR-36).");
        }
        return project;
    }

    private String requireEmail(String email, Project project) {
        String trimmed = email == null ? "" : email.trim();
        if (trimmed.isEmpty()) {
            throw new ProjectValidationException("Manager email is required.");
        }
        if (!managerDirectory.isManager(trimmed)) {
            throw new ProjectValidationException("\"" + trimmed
                    + "\" is not a registered Manager. Enter the email of an existing Manager (BR-19).");
        }
        return trimmed;
    }
}
