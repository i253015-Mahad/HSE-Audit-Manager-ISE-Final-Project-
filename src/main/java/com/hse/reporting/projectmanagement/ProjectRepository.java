package com.hse.reporting.projectmanagement;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository (ADR-04/ADR-08: MySQL + JPA).
 * Delegated to by {@link ProjectJpaStore}; not used directly by the service.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    boolean existsByName(String name);

    List<Project> findAllByOrderByNameAsc();

    /**
     * Project list filters (Admin UI): free text over name OR manager email,
     * combined with an optional state. Both parameters may be {@code null},
     * which means "no restriction" — blank input is normalized to null by the
     * store adapter so this query never sees an empty pattern.
     */
    @Query("""
        select p from Project p
        where (:q is null or lower(p.name) like lower(concat('%', :q, '%'))
            or lower(coalesce(p.managerEmail, '')) like lower(concat('%', :q, '%')))
          and (:state is null or p.state = :state)
        order by p.name asc
        """)
    List<Project> search(@Param("q") String q, @Param("state") Project.State state);

    /** Report creation (FR-04) reads only open projects handled by an Inspector's Manager. */
    List<Project> findByManagerEmailAndState(String managerEmail, Project.State state);

    @Override
    Optional<Project> findById(Long id);

    @Override
    <S extends Project> S save(S entity);
}
