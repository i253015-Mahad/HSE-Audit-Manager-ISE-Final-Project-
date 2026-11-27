package com.hse.reporting.useraccess.access;

import com.hse.reporting.projectmanagement.access.ProjectAccessQueries;
import com.hse.reporting.useraccess.domain.Role;
import org.springframework.stereotype.Service;

/**
 * Central authorization decisions of the system (ADR-03).
 *
 * <p>Access is <b>role-based combined with project-based restrictions</b>:
 * <ul>
 *   <li>Role permissions decide which operations a user may perform.</li>
 *   <li>Manager access is limited to projects assigned to that Manager.</li>
 *   <li>HSE Engineer/Admin oversight is cross-project (ASR-03).</li>
 *   <li>Inspector access to report data is ownership-based and is evaluated by
 *       Safety Report Management in later slices; the Inspector has no project scope.</li>
 * </ul>
 *
 * <p>This bean is the single place where access-policy decisions live; other modules
 * must use it instead of implementing a second access policy (AGENTS.md section 6).
 * Project assignment data is read through the Project Management interface
 * {@link ProjectAccessQueries} — never from a second copy of that data.
 *
 * <p>Authorization is enforced server-side on every protected use case (ASR-01);
 * hidden UI controls are never the only enforcement.
 */
@Service
public class AuthorizationService {

    private final ProjectAccessQueries projectAccess;

    public AuthorizationService(ProjectAccessQueries projectAccess) {
        this.projectAccess = projectAccess;
    }

    /**
     * Ensures the actor holds one of the allowed system roles.
     *
     * @throws AuthorizationDeniedException if the actor's role is not allowed
     */
    public void requireRole(Actor actor, Role... allowedRoles) {
        if (!hasRole(actor, allowedRoles)) {
            throw new AuthorizationDeniedException(
                    "User " + actor.email() + " with role " + actor.role()
                            + " is not permitted to perform this operation");
        }
    }

    /** @return whether the actor holds one of the allowed system roles */
    public boolean hasRole(Actor actor, Role... allowedRoles) {
        for (Role allowed : allowedRoles) {
            if (actor.role() == allowed) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ensures the actor may access data of the given project.
     *
     * @throws AuthorizationDeniedException if the project is outside the actor's scope
     */
    public void requireProjectAccess(Actor actor, long projectId) {
        if (!canAccessProject(actor, projectId)) {
            throw new AuthorizationDeniedException(
                    "User " + actor.email() + " may not access project " + projectId);
        }
    }

    /**
     * Project scope check (ADR-03): role <b>and</b> project assignment.
     *
     * <ul>
     *   <li>ADMIN — cross-project oversight: every project.</li>
     *   <li>MANAGER — only projects assigned to the Manager's email.</li>
     *   <li>INSPECTOR — no project scope (own-report access is evaluated by
     *       Safety Report Management in later slices).</li>
     * </ul>
     *
     * @return {@code true} if the actor may access data of the project
     */
    public boolean canAccessProject(Actor actor, long projectId) {
        return switch (actor.role()) {
            case ADMIN -> true;
            case MANAGER -> projectAccess.isAssignedManager(actor.email(), projectId);
            case INSPECTOR -> false;
        };
    }
}
