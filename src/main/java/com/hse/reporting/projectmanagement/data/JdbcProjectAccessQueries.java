package com.hse.reporting.projectmanagement.data;

import com.hse.reporting.projectmanagement.access.ProjectAccessQueries;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Implements {@link ProjectAccessQueries} with plain SQL over the tables owned by
 * the Project Management module (schema.sql).
 *
 * <p>Read-only and simple, so no repository abstraction is introduced yet
 * (ADR-02: repository layer only where database-access complexity justifies it).
 */
@Repository
public class JdbcProjectAccessQueries implements ProjectAccessQueries {

    private static final String PROJECT_COLUMNS = "p.id, p.name, (p.state = 'OPEN') AS project_open";

    private final JdbcTemplate jdbc;

    public JdbcProjectAccessQueries(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean isAssignedManager(String managerEmail, long projectId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM projects WHERE manager_email = ? AND id = ?",
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
                        + " WHERE p.manager_email = ?"
                        + " ORDER BY p.name",
                JdbcProjectAccessQueries::toSummary,
                managerEmail);
    }

    @Override
    public List<ProjectSummary> allProjects() {
        return jdbc.query(
                "SELECT " + PROJECT_COLUMNS + " FROM projects p ORDER BY p.name",
                JdbcProjectAccessQueries::toSummary);
    }

    @Override
    public List<ProjectSummary> projectsByIds(Collection<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return List.of();
        }
        String placeholders = projectIds.stream().map(id -> "?").collect(Collectors.joining(","));
        Object[] ids = projectIds.toArray();
        return jdbc.query(
                "SELECT " + PROJECT_COLUMNS + " FROM projects p WHERE p.id IN (" + placeholders + ") ORDER BY p.name",
                JdbcProjectAccessQueries::toSummary,
                ids);
    }

    private static ProjectSummary toSummary(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new ProjectSummary(rs.getLong("id"), rs.getString("name"), rs.getBoolean("project_open"));
    }
}
