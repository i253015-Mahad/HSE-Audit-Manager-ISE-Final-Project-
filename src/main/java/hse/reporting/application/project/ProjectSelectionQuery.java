package hse.reporting.application.project;

import hse.reporting.project.Project;
import hse.reporting.project.ProjectRepository;
import hse.reporting.user.AppUser;
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
        AppUser actor = currentActor.currentUser();
        if (actor.getManagerEmail() == null || actor.getManagerEmail().isBlank()) {
            return List.of();
        }
        return projects.findByManagerEmailAndClosedFalse(actor.getManagerEmail()).stream()
            .map(project -> new ProjectOption(project.getId(), project.getName()))
            .toList();
    }

    public record ProjectOption(Long id, String name) {
    }
}
