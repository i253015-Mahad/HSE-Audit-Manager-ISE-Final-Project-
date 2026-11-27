package hse.reporting.project;

import com.hse.reporting.projectmanagement.Project;
import com.hse.reporting.projectmanagement.Project.State;
import com.hse.reporting.projectmanagement.ProjectRepository;
import hse.reporting.application.ports.ProjectEligibilityPort;
import hse.reporting.user.InspectorDesignation;
import hse.reporting.user.InspectorDesignationRepository;
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
    private InspectorDesignationRepository designations;

    private ProjectEligibilityService service;

    @BeforeEach
    void setUp() {
        service = new ProjectEligibilityService(projects, designations);
    }

    @Test
    void acceptsProjectHandledByInspectorsManager() {
        when(projects.findById(1L)).thenReturn(Optional.of(project("Site A", "manager@hse.test", State.OPEN)));
        when(designations.findById("inspector@hse.test"))
            .thenReturn(Optional.of(new InspectorDesignation("inspector@hse.test", "manager@hse.test")));

        assertTrue(service.check("inspector@hse.test", 1L).allowed());
    }

    @Test
    void refusesProjectHandledByAnotherManager() {
        when(projects.findById(1L)).thenReturn(Optional.of(project("Site A", "other@hse.test", State.OPEN)));
        when(designations.findById("inspector@hse.test"))
            .thenReturn(Optional.of(new InspectorDesignation("inspector@hse.test", "manager@hse.test")));

        ProjectEligibilityPort.Eligibility result = service.check("inspector@hse.test", 1L);
        assertFalse(result.allowed());
        assertEquals("The project is not handled by your Manager.", result.refusalMessage());
    }

    @Test
    void refusesClosedOrManagerlessProject() {
        when(projects.findById(1L)).thenReturn(Optional.of(project("Site B", null, State.OPEN)));
        when(projects.findById(2L)).thenReturn(Optional.of(project("Site C", "manager@hse.test", State.CLOSED)));

        assertFalse(service.check("inspector@hse.test", 1L).allowed());
        assertFalse(service.check("inspector@hse.test", 2L).allowed());
    }

    @Test
    void refusesUnknownProject() {
        when(projects.findById(1L)).thenReturn(Optional.empty());

        assertFalse(service.check("inspector@hse.test", 1L).allowed());
    }

    private Project project(String name, String managerEmail, State state) {
        Project project = new Project(name);
        project.setManagerEmail(managerEmail);
        project.setState(state);
        return project;
    }
}
