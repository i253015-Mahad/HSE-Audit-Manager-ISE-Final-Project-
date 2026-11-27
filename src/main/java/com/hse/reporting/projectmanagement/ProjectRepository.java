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

    @Override
    Optional<Project> findById(Long id);

    @Override
    <S extends Project> S save(S entity);
}
