package hse.reporting.application.project;

import com.hse.reporting.projectmanagement.Project;
import com.hse.reporting.projectmanagement.ProjectRepository;
import hse.reporting.user.CurrentActorService;
import org.springframework.stereotype.Service;

import java.util.List;

/** Supplies the project choices that satisfy FR-04 for the current Inspector. */
@Service
public class ProjectSelectionQuery {

    private final ProjectRepository projects;
    private final CurrentActorService currentActor;

    public ProjectSelectionQuery(ProjectRepository projects, CurrentActorService currentActor) {
        this.projects = projects;
        this.currentActor = currentActor;
    }

    public List<ProjectOption> availableForCurrentInspector() {
        String managerEmail = currentActor.managerEmail();
        if (managerEmail == null || managerEmail.isBlank()) {
            return List.of();
        }
        return projects.findByManagerEmailAndState(managerEmail, Project.State.OPEN).stream()
            .map(project -> new ProjectOption(project.getId(), project.getName(), project.getManagerEmail()))
            .toList();
    }

    public record ProjectOption(Long id, String name, String managerEmail) {
    }
}
