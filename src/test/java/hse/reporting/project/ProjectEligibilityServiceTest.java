package hse.reporting.project;

import hse.reporting.application.ports.ProjectEligibilityPort;
import hse.reporting.user.AppUser;
import hse.reporting.user.Role;
import hse.reporting.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectEligibilityServiceTest {

    @Mock
    private ProjectRepository projects;

    @Mock
    private UserRepository users;

    private ProjectEligibilityService service;

    @BeforeEach
    void setUp() {
        service = new ProjectEligibilityService(projects, users);
    }

    @Test
    void acceptsProjectHandledByInspectorsManager() {
        when(projects.findById(1L)).thenReturn(Optional.of(new Project("Site A", "manager@hse.test", false)));
        when(users.findByEmail("inspector@hse.test"))
            .thenReturn(Optional.of(new AppUser("inspector@hse.test", "hash", Role.INSPECTOR, "manager@hse.test")));

        assertTrue(service.check("inspector@hse.test", 1L).allowed());
    }

    @Test
    void refusesProjectHandledByAnotherManager() {
        when(projects.findById(1L)).thenReturn(Optional.of(new Project("Site A", "other@hse.test", false)));
        when(users.findByEmail("inspector@hse.test"))
            .thenReturn(Optional.of(new AppUser("inspector@hse.test", "hash", Role.INSPECTOR, "manager@hse.test")));

        ProjectEligibilityPort.Eligibility result = service.check("inspector@hse.test", 1L);
        assertFalse(result.allowed());
        assertEquals("The project is not handled by your Manager.", result.refusalMessage());
    }

    @Test
    void refusesClosedOrManagerlessProject() {
        when(projects.findById(1L)).thenReturn(Optional.of(new Project("Site B", null, false)));

        assertFalse(service.check("inspector@hse.test", 1L).allowed());
    }

    @Test
    void refusesUnknownProject() {
        when(projects.findById(1L)).thenReturn(Optional.empty());

        assertFalse(service.check("inspector@hse.test", 1L).allowed());
    }
}
