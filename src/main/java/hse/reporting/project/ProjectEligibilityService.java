package hse.reporting.project;

import com.hse.reporting.projectmanagement.Project;
import com.hse.reporting.projectmanagement.ProjectRepository;
import hse.reporting.application.ports.ProjectEligibilityPort;
import hse.reporting.user.InspectorDesignation;
import hse.reporting.user.InspectorDesignationRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/** FR-04: exists, has a Manager, is not closed, and belongs to the Inspector's Manager. */
@Service
public class ProjectEligibilityService implements ProjectEligibilityPort {

    private final ProjectRepository projects;
    private final InspectorDesignationRepository designations;

    public ProjectEligibilityService(ProjectRepository projects,
                                     InspectorDesignationRepository designations) {
        this.projects = projects;
        this.designations = designations;
    }

    @Override
    public Eligibility check(String inspectorEmail, Long projectId) {
        if (projectId == null) {
            return Eligibility.refuse("Select a project.");
        }

        Optional<Project> found = projects.findById(projectId);
        if (found.isEmpty()) {
            return Eligibility.refuse("No project matches the selected project.");
        }
        Project project = found.get();

        if (project.getManagerEmail() == null || project.getManagerEmail().isBlank()) {
            return Eligibility.refuse("The project has no assigned Manager, so a report cannot be submitted.");
        }
        if (!project.isOpen()) {
            return Eligibility.refuse("The project is closed, so a report cannot be submitted against it.");
        }

        InspectorDesignation designation = designations.findById(inspectorEmail).orElse(null);
        if (designation == null || designation.getManagerEmail() == null
            || designation.getManagerEmail().isBlank()) {
            return Eligibility.refuse("Your Manager designation is missing; contact your administrator.");
        }
        if (!project.getManagerEmail().equalsIgnoreCase(designation.getManagerEmail())) {
            return Eligibility.refuse("The project is not handled by your Manager.");
        }
        return Eligibility.permit();
    }
}
