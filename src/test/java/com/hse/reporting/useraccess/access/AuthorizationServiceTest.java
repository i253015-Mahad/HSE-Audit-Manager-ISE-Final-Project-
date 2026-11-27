package com.hse.reporting.useraccess.access;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hse.reporting.projectmanagement.access.ProjectAccessQueries;
import com.hse.reporting.useraccess.domain.Role;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the authorization decisions of ADR-03: role-based permissions
 * combined with project-based restrictions (project scope via the Project
 * Management interface, never a second copy of assignment data).
 */
class AuthorizationServiceTest {

    private static final String ASSIGNED_MANAGER = "mahadwaqas28@gmail.com";
    private static final long ASSIGNED_PROJECT = 1L;
    private static final long UNASSIGNED_PROJECT = 3L;

    private final AuthorizationService authorization =
            new AuthorizationService(new ProjectAccessQueries() {

                @Override
                public boolean isAssignedManager(String managerEmail, long projectId) {
                    return ASSIGNED_MANAGER.equals(managerEmail) && projectId == ASSIGNED_PROJECT;
                }

                @Override
                public List<ProjectSummary> projectsAssignedTo(String managerEmail) {
                    return List.of();
                }

                @Override
                public List<ProjectSummary> allProjects() {
                    return List.of();
                }
            });

    // ---- role checks ------------------------------------------------------

    @Test
    void allowedRolePasses() {
        Actor admin = new Actor("manal12cf@gmail.com", Role.ADMIN);
        authorization.requireRole(admin, Role.ADMIN, Role.MANAGER); // must not throw
    }

    @Test
    void wrongRoleIsDenied() {
        Actor inspector = new Actor("i253172@nu.edu.pk", Role.INSPECTOR);
        assertThatThrownBy(() -> authorization.requireRole(inspector, Role.ADMIN))
                .isInstanceOf(AuthorizationDeniedException.class);
    }

    @Test
    void managerDoesNotGainAdminRights() {
        Actor manager = new Actor(ASSIGNED_MANAGER, Role.MANAGER);
        assertThat(authorization.hasRole(manager, Role.ADMIN)).isFalse();
        assertThat(authorization.hasRole(manager, Role.MANAGER)).isTrue();
    }

    // ---- project scope ----------------------------------------------------

    @Test
    void adminHasCrossProjectOversight() {
        Actor admin = new Actor("manal12cf@gmail.com", Role.ADMIN);
        assertThat(authorization.canAccessProject(admin, ASSIGNED_PROJECT)).isTrue();
        assertThat(authorization.canAccessProject(admin, UNASSIGNED_PROJECT)).isTrue();
    }

    @Test
    void managerAccessIsLimitedToAssignedProjects() {
        Actor manager = new Actor(ASSIGNED_MANAGER, Role.MANAGER);
        assertThat(authorization.canAccessProject(manager, ASSIGNED_PROJECT)).isTrue();
        assertThat(authorization.canAccessProject(manager, UNASSIGNED_PROJECT)).isFalse();
    }

    @Test
    void anotherManagerHasNoAccessToThatProject() {
        Actor otherManager = new Actor("extra.manager@hse.local", Role.MANAGER);
        assertThat(authorization.canAccessProject(otherManager, ASSIGNED_PROJECT)).isFalse();
    }

    @Test
    void inspectorHasNoProjectScope() {
        Actor inspector = new Actor("i253172@nu.edu.pk", Role.INSPECTOR);
        assertThat(authorization.canAccessProject(inspector, ASSIGNED_PROJECT)).isFalse();
        assertThatThrownBy(() -> authorization.requireProjectAccess(inspector, ASSIGNED_PROJECT))
                .isInstanceOf(AuthorizationDeniedException.class);
    }

    @Test
    void unassignedManagerCannotAccessProject() {
        Actor manager = new Actor(ASSIGNED_MANAGER, Role.MANAGER);
        assertThatThrownBy(() -> authorization.requireProjectAccess(manager, UNASSIGNED_PROJECT))
                .isInstanceOf(AuthorizationDeniedException.class);
    }
}
