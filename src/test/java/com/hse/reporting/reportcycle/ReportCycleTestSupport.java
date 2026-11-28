package com.hse.reporting.reportcycle;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.hse.reporting.projectmanagement.ProjectService;

import hse.reporting.report.SafetyReport;
import hse.reporting.report.SafetyReportRepository;

/**
 * Shared plumbing for the Slice 4 report-cycle integration tests.
 *
 * <p>The H2 test database is SHARED across every test class (one Spring
 * context), and the baseline tests assert on empty dashboards and on the seed
 * project names being absent from filtered views. Therefore this support class:
 *
 * <ul>
 *   <li>creates DEDICATED cycle accounts and projects per test (never touches
 *       the seed rows for projects 1–3 or the three demo users),</li>
 *   <li>tracks every row it creates and deletes it again in
 *       {@code @AfterEach} (the schema has no foreign keys, so plain DELETEs
 *       in child-first order are enough),</li>
 *   <li>offers submit/approve/reject helpers that speak the exact HTTP
 *       contract of the application (form POST + CSRF + per-request identity).
 * </ul>
 */
abstract class ReportCycleTestSupport {

    static final String ADMIN_EMAIL = "manal12cf@gmail.com";
    static final String MANAGER_EMAIL = "mahadwaqas28@gmail.com";

    /** The acting cycle Inspector — designated to the cycle Manager. */
    static final String INSPECTOR_EMAIL = "cycle.inspector@hse.local";
    /** A second Inspector on the same Manager, used for "not mine" assertions. */
    static final String OTHER_INSPECTOR_EMAIL = "cycle.inspector2@hse.local";
    /** The cycle Manager — owns every project this support class creates. */
    static final String CYCLE_MANAGER_EMAIL = "cycle.manager@hse.local";
    /** A Manager of a different project, used for BR-04 (wrong-manager) checks. */
    static final String OTHER_MANAGER_EMAIL = "cycle.manager2@hse.local";

    private static final String DEMO_PASSWORD_HASH =
        "$2a$10$TaSHkhCQXUiofSNBnBdKweYgXl.HNlzXFuvpiFEhLoDoT76atl1JO";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected SafetyReportRepository reports;

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    protected ProjectService projectService;

    private final List<Long> createdReportIds = new ArrayList<>();
    private final List<String> createdEmails = new ArrayList<>();
    private final List<Long> createdProjectIds = new ArrayList<>();

    // ------------------------------------------------------------------
    // lifecycle
    // ------------------------------------------------------------------

    /** Cycle accounts are re-inserted (guarded) before every test. */
    @org.junit.jupiter.api.BeforeEach
    void seedCycleAccounts() {
        insertUserIfAbsent(CYCLE_MANAGER_EMAIL, "MANAGER");
        insertUserIfAbsent(OTHER_MANAGER_EMAIL, "MANAGER");
        insertUserIfAbsent(INSPECTOR_EMAIL, "INSPECTOR");
        insertUserIfAbsent(OTHER_INSPECTOR_EMAIL, "INSPECTOR");
        remember(INSPECTOR_EMAIL, OTHER_INSPECTOR_EMAIL, CYCLE_MANAGER_EMAIL, OTHER_MANAGER_EMAIL);

        insertIfAbsent(
            "SELECT COUNT(*) FROM inspector_designation WHERE inspector_email = ?", INSPECTOR_EMAIL,
            () -> jdbc.update(
                "INSERT INTO inspector_designation (inspector_email, manager_email) VALUES (?, ?)",
                INSPECTOR_EMAIL, CYCLE_MANAGER_EMAIL));
        insertIfAbsent(
            "SELECT COUNT(*) FROM inspector_designation WHERE inspector_email = ?", OTHER_INSPECTOR_EMAIL,
            () -> jdbc.update(
                "INSERT INTO inspector_designation (inspector_email, manager_email) VALUES (?, ?)",
                OTHER_INSPECTOR_EMAIL, CYCLE_MANAGER_EMAIL));
        insertIfAbsent(
            "SELECT COUNT(*) FROM observer_profile WHERE email = ?", INSPECTOR_EMAIL,
            () -> jdbc.update(
                "INSERT INTO observer_profile (email, full_name, badge_no, title, company) VALUES (?, ?, ?, ?, ?)",
                INSPECTOR_EMAIL, "Cycle Inspector", "I-9001", "Safety Inspector", "HSE Department"));
        insertIfAbsent(
            "SELECT COUNT(*) FROM observer_profile WHERE email = ?", OTHER_INSPECTOR_EMAIL,
            () -> jdbc.update(
                "INSERT INTO observer_profile (email, full_name, badge_no, title, company) VALUES (?, ?, ?, ?, ?)",
                OTHER_INSPECTOR_EMAIL, "Cycle Inspector Two", "I-9002", "Safety Inspector", "HSE Department"));
    }

    @AfterEach
    void removeCycleRows() {
        for (Long reportId : createdReportIds) {
            jdbc.update("DELETE FROM photograph_evidence WHERE violation_id IN "
                + "(SELECT id FROM safety_violation WHERE report_id = ?)", reportId);
            jdbc.update("DELETE FROM safety_violation WHERE report_id = ?", reportId);
            jdbc.update("DELETE FROM report_version WHERE report_id = ?", reportId);
            jdbc.update("DELETE FROM notification WHERE report_id = ?", reportId);
            jdbc.update("DELETE FROM submission_token WHERE report_id = ?", reportId);
            jdbc.update("DELETE FROM safety_report WHERE id = ?", reportId);
        }
        for (String email : createdEmails) {
            jdbc.update("DELETE FROM inspector_designation WHERE inspector_email = ?", email);
            jdbc.update("DELETE FROM observer_profile WHERE email = ?", email);
            jdbc.update("DELETE FROM users WHERE email = ?", email);
        }
        for (Long projectId : createdProjectIds) {
            jdbc.update("DELETE FROM projects WHERE id = ?", projectId);
        }
        createdReportIds.clear();
        createdEmails.clear();
        createdProjectIds.clear();
    }

    // ------------------------------------------------------------------
    // fixtures
    // ------------------------------------------------------------------

    /** A fresh OPEN project owned by the cycle Manager. */
    protected long createProject() {
        String name = "Cycle " + UUID.randomUUID().toString().substring(0, 8);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        // No try-with-resources here: the framework executes the statement AFTER
        // this creator returns, so Spring owns (and closes) its lifecycle.
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO projects (name, manager_email, state) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, name);
            statement.setString(2, CYCLE_MANAGER_EMAIL);
            statement.setString(3, "OPEN");
            return statement;
        }, keyHolder);
        long id = keyHolder.getKey().longValue();
        createdProjectIds.add(id);
        return id;
    }

    /** A fresh OPEN project owned by the OTHER Manager (BR-04 fixtures). */
    protected long createProjectForOtherManager() {
        String name = "Cycle " + UUID.randomUUID().toString().substring(0, 8);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO projects (name, manager_email, state) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, name);
            statement.setString(2, OTHER_MANAGER_EMAIL);
            statement.setString(3, "OPEN");
            return statement;
        }, keyHolder);
        long id = keyHolder.getKey().longValue();
        createdProjectIds.add(id);
        return id;
    }

    // ------------------------------------------------------------------
    // HTTP helpers — the exact form contract of the application
    // ------------------------------------------------------------------

    /** Submits one SOR as the cycle Inspector and returns the persisted report id. */
    protected long submitReport(long projectId, String location) throws Exception {
        return submitReportAs(INSPECTOR_EMAIL, projectId, location, "INSPECTION_FINDING", "High");
    }

    protected long submitReportAs(String inspectorEmail, long projectId, String location,
                                  String observationType, String riskRating) throws Exception {
        String token = submissionTokenFor(inspectorEmail);
        mockMvc.perform(post("/reports")
                        .with(user(inspectorEmail).roles("INSPECTOR"))
                        .with(csrf())
                        .param("submissionToken", token)
                        .param("projectId", String.valueOf(projectId))
                        .param("observationDate", "2026-11-27")
                        .param("observationTime", "09:30")
                        .param("location", location)
                        .param("observationType", observationType)
                        .param("violations[0].briefDescription", "Unprotected edge " + location)
                        .param("violations[0].observationHazard", "Unsafe condition")
                        .param("violations[0].riskRating", riskRating)
                        .param("violations[0].actionRecommended", "Install guardrails")
                        .param("violations[0].violatorName", "Cycle Violator")
                        .param("violations[0].violatorBadgeNo", "B-777")
                        .param("violations[0].violatorTitle", "Labourer")
                        .param("violations[0].taskIncharge", "Cycle Foreman"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reports/submitted"));

        SafetyReport saved = reports.findAll().stream()
            .filter(report -> location.equals(report.getLocation()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Submitted SOR was not persisted: " + location));
        createdReportIds.add(saved.getId());
        return saved.getId();
    }

    /** Submits one SOR including a real JPEG photograph; returns the report id. */
    protected long submitReportWithPhoto(long projectId, String location) throws Exception {
        String token = submissionTokenFor(INSPECTOR_EMAIL);
        MockMultipartFile photo = new MockMultipartFile(
            "violations[0].photos", "edge.jpg", "image/jpeg", jpegBytes());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/reports")
                        .file(photo)
                        .with(user(INSPECTOR_EMAIL).roles("INSPECTOR"))
                        .with(csrf())
                        .param("submissionToken", token)
                        .param("projectId", String.valueOf(projectId))
                        .param("observationDate", "2026-11-27")
                        .param("observationTime", "10:00")
                        .param("location", location)
                        .param("observationType", "ACCIDENT")
                        .param("violations[0].briefDescription", "Photo edge " + location)
                        .param("violations[0].observationHazard", "Unsafe condition")
                        .param("violations[0].riskRating", "Critical")
                        .param("violations[0].actionRecommended", "Install guardrails")
                        .param("violations[0].violatorName", "Cycle Violator")
                        .param("violations[0].violatorBadgeNo", "B-777")
                        .param("violations[0].violatorTitle", "Labourer")
                        .param("violations[0].taskIncharge", "Cycle Foreman"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reports/submitted"));

        SafetyReport saved = reports.findAll().stream()
            .filter(report -> location.equals(report.getLocation()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Photo SOR was not persisted: " + location));
        createdReportIds.add(saved.getId());
        return saved.getId();
    }

    /** Minimal JPEG: SOI FFD8FF + JFIF marker — enough for PhotoFormat's magic check. */
    protected static byte[] jpegBytes() {
        byte[] bytes = new byte[32];
        bytes[0] = (byte) 0xFF;
        bytes[1] = (byte) 0xD8;
        bytes[2] = (byte) 0xFF;
        bytes[3] = (byte) 0xE0;
        byte[] jfif = "JFIF\0".getBytes();
        System.arraycopy(jfif, 0, bytes, 6, jfif.length);
        return bytes;
    }

    protected void approve(long reportId) throws Exception {
        mockMvc.perform(post("/reports/" + reportId + "/approve")
                        .with(user(CYCLE_MANAGER_EMAIL).roles("MANAGER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    protected void reject(long reportId, String reason) throws Exception {
        mockMvc.perform(post("/reports/" + reportId + "/reject")
                        .with(user(CYCLE_MANAGER_EMAIL).roles("MANAGER"))
                        .with(csrf())
                        .param("rejectReason", reason))
                .andExpect(status().is3xxRedirection());
    }

    protected String reference(long reportId) {
        return String.format("RPT-%04d", reportId);
    }

    protected SafetyReport report(long reportId) {
        return reports.findByIdAndDeletedFalse(reportId)
            .orElseThrow(() -> new AssertionError("Report disappeared: " + reportId));
    }

    protected int notificationCount(long reportId) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM notification WHERE report_id = ?", Integer.class, reportId);
        return count == null ? 0 : count;
    }

    protected int versionCount(long reportId) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM report_version WHERE report_id = ?", Integer.class, reportId);
        return count == null ? 0 : count;
    }

    // ------------------------------------------------------------------
    // internals
    // ------------------------------------------------------------------

    private String submissionTokenFor(String inspectorEmail) throws Exception {
        String form = mockMvc.perform(get("/reports/new")
                        .with(user(inspectorEmail).roles("INSPECTOR")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Matcher matcher = Pattern.compile("name=\"submissionToken\"[^>]*value=\"([^\"]+)\"")
            .matcher(form);
        if (!matcher.find()) {
            throw new AssertionError("submission token missing for " + inspectorEmail);
        }
        return matcher.group(1);
    }

    private void insertUserIfAbsent(String email, String role) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, email);
        if (count == null || count == 0) {
            jdbc.update("INSERT INTO users (email, password_hash, role) VALUES (?, ?, ?)",
                email, DEMO_PASSWORD_HASH, role);
        }
    }

    private void insertIfAbsent(String countQuery, Object key, Runnable insert) {
        Integer count = jdbc.queryForObject(countQuery, Integer.class, key);
        if (count == null || count == 0) {
            insert.run();
        }
    }

    private void remember(String... emails) {
        for (String email : emails) {
            if (!createdEmails.contains(email)) {
                createdEmails.add(email);
            }
        }
    }
}
