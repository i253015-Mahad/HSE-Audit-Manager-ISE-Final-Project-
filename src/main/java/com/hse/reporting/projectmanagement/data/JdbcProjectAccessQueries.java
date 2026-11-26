package com.hse.reporting.projectmanagement.data;

import com.hse.reporting.projectmanagement.access.ProjectAccessQueries;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Implements {@link ProjectAccessQueries} with plain SQL over the tables owned by
 * the Project Management module (schema.sql).
 *
 * <p>Read-only and simple, so no repository abstraction is introduced yet
 * (ADR-02: repository layer only where database-access complexity justifies it).
 * Slice 5 adds the write-side project management use cases.
 */
@Repository
public class JdbcProjectAccessQueries implements ProjectAccessQueries {

    private static final String PROJECT_COLUMNS = "p.id, p.name";

    private final JdbcTemplate jdbc;

    public JdbcProjectAccessQueries(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean isAssignedManager(String managerEmail, long projectId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM project_manager WHERE manager_email = ? AND project_id = ?",
                Integer.class,
                managerEmail,
                projectId);
        return count != null && count > 0;
    }

    @Override
    public List<ProjectSummary> projectsAssignedTo(String managerEmail) {
        return jdbc.query(
                "SELECT " + PROJECT_COLUMNS
                        + " FROM projects p"
                        + " JOIN project_manager pm ON pm.project_id = p.id"
                        + " WHERE pm.manager_email = ?"
                        + " ORDER BY p.name",
                (rs, rowNum) -> new ProjectSummary(rs.getLong("id"), rs.getString("name")),
                managerEmail);
    }

    @Override
    public List<ProjectSummary> allProjects() {
        return jdbc.query(
                "SELECT " + PROJECT_COLUMNS + " FROM projects p ORDER BY p.name",
                (rs, rowNum) -> new ProjectSummary(rs.getLong("id"), rs.getString("name")));
    }
}
