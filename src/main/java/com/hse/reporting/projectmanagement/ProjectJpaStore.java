package com.hse.reporting.projectmanagement;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter for {@link ProjectStore} — the only class that talks to
 * {@link ProjectRepository} (AGENTS.md §6: capabilities own their data).
 */
@Repository
public class ProjectJpaStore implements ProjectStore {

    private final ProjectRepository repository;

    public ProjectJpaStore(ProjectRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByName(String name) {
        return repository.existsByName(name);
    }

    @Override
    public Optional<Project> find(Long id) {
        return repository.findById(id);
    }

    @Override
    public Project save(Project project) {
        return repository.save(project);
    }

    @Override
    public List<Project> findAll() {
        return repository.findAllByOrderByNameAsc();
    }

    @Override
    public List<Project> search(String q, Project.State state) {
        String text = q == null || q.isBlank() ? null : q.trim();
        return repository.search(text, state);
    }
}
