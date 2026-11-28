package com.hse.reporting.projectmanagement;

/**
 * Narrow persistence port used by {@link ProjectService}.
 *
 * <p>Deliberately tiny so the service can be unit-tested with an in-memory fake
 * before Manal's application skeleton (pom.xml, Spring context) lands. The JPA
 * adapter is {@link ProjectJpaStore}.
 */
public interface ProjectStore {

    boolean existsByName(String name);

    java.util.Optional<Project> find(Long id);

    Project save(Project project);

    java.util.List<Project> findAll();

    /** Filtered listing for the Admin project list (text over name/manager + optional state). */
    java.util.List<Project> search(String q, Project.State state);
}
