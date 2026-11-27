package com.hse.reporting.projectmanagement.access;

import java.util.List;

/**
 * Read-only project information exposed by the Project Management module to
 * other modules — principally the project-scope half of authorization
 * (architecture.md section 5.3.B: Project Management "provides project-state and
 * project-access information to other modules").
 *
 * <p>Callers receive this capability through this interface only; the underlying
 * tables (projects) are private to Project Management
 * (AGENTS.md section 6). Writes (create project, assign/reassign Manager, close
 * project) belong to Slice 5 and are not exposed here.
 */
public interface ProjectAccessQueries {

    /** Lightweight, non-authoritative projection of a project for display and scope checks. */
    record ProjectSummary(long id, String name) {
    }

    /**
     * Whether the given email is the currently assigned Manager of the project
     * (permission note 6: reassignment transfers these permissions automatically).
     */
    boolean isAssignedManager(String managerEmail, long projectId);

    /** Projects currently assigned to the given Manager, for that Manager's own view. */
    List<ProjectSummary> projectsAssignedTo(String managerEmail);

    /** All projects — used for HSE Engineer/Admin cross-project oversight (ASR-03). */
    List<ProjectSummary> allProjects();
}
