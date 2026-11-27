package hse.reporting.project;

import hse.reporting.application.ports.ProjectEligibilityPort;
import hse.reporting.user.AppUser;
import hse.reporting.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/** FR-04: exists, has a Manager, is not closed, and belongs to the Inspector's Manager. */
@Service
public class ProjectEligibilityService implements ProjectEligibilityPort {

    private final ProjectRepository projects;
    private final UserRepository users;

    public ProjectEligibilityService(ProjectRepository projects, UserRepository users) {
        this.projects = projects;
        this.users = users;
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
        if (project.isClosed()) {
            return Eligibility.refuse("The project is closed, so a report cannot be submitted against it.");
        }

        AppUser inspector = users.findByEmail(inspectorEmail).orElse(null);
        if (inspector == null || inspector.getManagerEmail() == null) {
            return Eligibility.refuse("Your Manager designation is missing; contact your administrator.");
        }
        if (!project.getManagerEmail().equalsIgnoreCase(inspector.getManagerEmail())) {
            return Eligibility.refuse("The project is not handled by your Manager.");
        }
        return Eligibility.permit();
    }
}
