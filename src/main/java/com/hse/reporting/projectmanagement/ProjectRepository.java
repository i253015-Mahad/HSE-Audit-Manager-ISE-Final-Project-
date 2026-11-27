package com.hse.reporting.projectmanagement;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository (ADR-04/ADR-08: MySQL + JPA).
 * Delegated to by {@link ProjectJpaStore}; not used directly by the service.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    boolean existsByName(String name);

    List<Project> findAllByOrderByNameAsc();

    /** Report creation (FR-04) reads only open projects handled by an Inspector's Manager. */
    List<Project> findByManagerEmailAndState(String managerEmail, Project.State state);

    @Override
    Optional<Project> findById(Long id);

    @Override
    <S extends Project> S save(S entity);
}
