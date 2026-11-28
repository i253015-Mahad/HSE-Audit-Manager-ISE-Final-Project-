package com.hse.reporting.application;

import com.hse.reporting.application.report.ReportViewingUseCase;
import com.hse.reporting.projectmanagement.access.ProjectAccessQueries;
import com.hse.reporting.projectmanagement.access.ProjectAccessQueries.ProjectSummary;
import com.hse.reporting.useraccess.access.Actor;
import com.hse.reporting.useraccess.access.AuthorizationService;
import com.hse.reporting.useraccess.domain.Role;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Application use case for the signed-in landing page of each role
 * (FR-40: the interface is determined by the logged-in user's role).
 *
 * <p>Coordinates: identifies the acting user ({@link ActorContext}),
 * invokes authorization ({@code AuthorizationService}) and gathers the
 * data the presentation layer is allowed to display. The presentation layer
 * never makes the access decision itself (ASR-01).
 */
@Service
public class DashboardUseCase {

    private final ActorContext actorContext;
    private final AuthorizationService authorization;
    private final ProjectAccessQueries projectAccess;
    private final ReportViewingUseCase reports;

    public DashboardUseCase(ActorContext actorContext,
                           AuthorizationService authorization,
                           ProjectAccessQueries projectAccess,
                           ReportViewingUseCase reports) {
        this.actorContext = actorContext;
        this.authorization = authorization;
        this.projectAccess = projectAccess;
        this.reports = reports;
    }

    /**
     * Inspector landing page: sees their own reports (pending, rejected, Open, Closed).
     */
    public InspectorDashboard inspectorDashboard() {
        Actor actor = actorContext.currentActor();
        authorization.requireRole(actor, Role.INSPECTOR);
        return new InspectorDashboard(reports.listForInspector());
    }

    /**
     * Manager landing page: the projects assigned to this Manager only
     * (ADR-03 — a Manager never sees projects they are not assigned to).
     */
    public ManagerDashboard managerDashboard() {
        Actor actor = actorContext.currentActor();
        authorization.requireRole(actor, Role.MANAGER);
        return new ManagerDashboard(projectAccess.projectsAssignedTo(actor.email()));
    }

    /** Admin/HSE Engineer landing page: cross-project oversight (ASR-03). */
    public AdminDashboard adminDashboard() {
        Actor actor = actorContext.currentActor();
        authorization.requireRole(actor, Role.ADMIN);
        return new AdminDashboard(projectAccess.allProjects());
    }

    public record ManagerDashboard(List<ProjectSummary> assignedProjects) {
    }

    public record AdminDashboard(List<ProjectSummary> projects) {
    }

    public record InspectorDashboard(List<com.hse.reporting.report.SafetyReport> reports) {
    }
}
