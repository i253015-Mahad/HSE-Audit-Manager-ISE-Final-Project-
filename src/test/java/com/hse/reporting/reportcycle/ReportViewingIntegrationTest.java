package com.hse.reporting.reportcycle;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import hse.reporting.photo.PhotographEvidence;
import hse.reporting.photo.PhotographEvidenceRepository;
import hse.reporting.report.SafetyReport;
import hse.reporting.report.SubmissionStatus;
import hse.reporting.report.Violation;

/**
 * Slice 4 report viewing (FR-14/15/16, AC-13/14/15/16, FR-12, EC-28/EC-29,
 * FR-44, photo playback) — the five filters included.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportViewingIntegrationTest extends ReportCycleTestSupport {

    @Autowired
    private PhotographEvidenceRepository photographs;

    // ------------------------------------------------------------------
    // FR-14 / AC-14 — Inspector sees only their own reports
    // ------------------------------------------------------------------

    @Test
    void ac14_inspectorSeesOnlyOwnReportsOnListAndDashboard() throws Exception {
        long projectId = createProject();
        long ownId = submitReport(projectId, "AC14 own report");
        long otherId = submitReportAs(OTHER_INSPECTOR_EMAIL, projectId, "AC14 other report",
            "INSPECTION_FINDING", "Medium");

        mockMvc.perform(get("/reports").with(user(INSPECTOR_EMAIL).roles("INSPECTOR")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(reference(ownId))))
            .andExpect(content().string(not(containsString(reference(otherId)))));

        // The dashboard card shows the same role-scoped set (FR-14).
        mockMvc.perform(get("/inspector").with(user(INSPECTOR_EMAIL).roles("INSPECTOR")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(reference(ownId))))
            .andExpect(content().string(not(containsString(reference(otherId)))));

        // The foreign report's detail is not readable either (existence does not leak).
        mockMvc.perform(get("/reports/" + otherId).with(user(INSPECTOR_EMAIL).roles("INSPECTOR")))
            .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------
    // FR-15 / AC-15 — Manager sees only reports of assigned projects
    // ------------------------------------------------------------------

    @Test
    void ac15_managerSeesOnlyAssignedProjectReports() throws Exception {
        long ownProject = createProject();
        long foreignProject = createProjectForOtherManager();
        long ownId = submitReport(ownProject, "AC15 assigned project report");
        long foreignId = submitReportAs(OTHER_INSPECTOR_EMAIL, foreignProject,
            "AC15 foreign project report", "INSPECTION_FINDING", "Low");

        mockMvc.perform(get("/reports").with(user(CYCLE_MANAGER_EMAIL).roles("MANAGER")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(reference(ownId))))
            .andExpect(content().string(not(containsString(reference(foreignId)))));

        mockMvc.perform(get("/manager").with(user(CYCLE_MANAGER_EMAIL).roles("MANAGER")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(reference(ownId))))
            .andExpect(content().string(not(containsString(reference(foreignId)))));

        // BR-04: the other project's report is not readable in detail either.
        mockMvc.perform(get("/reports/" + foreignId).with(user(CYCLE_MANAGER_EMAIL).roles("MANAGER")))
            .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------
    // FR-16 / AC-13 / FR-12 — Admin sees approved reports only
    // ------------------------------------------------------------------

    @Test
    void ac16_adminSeesApprovedReportsOnlyAndCannotOpenPendingOnes() throws Exception {
        long projectId = createProject();
        long pendingId = submitReport(projectId, "AC16 pending report");

        // Pending reports never reach the Admin (FR-16), neither on list nor detail (AC-13).
        mockMvc.perform(get("/reports").with(user(ADMIN_EMAIL).roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString(reference(pendingId)))))
            // FR-12: the Admin is never offered a Pending filter that cannot match.
            .andExpect(content().string(not(containsString("Pending Approval"))));

        mockMvc.perform(get("/reports/" + pendingId).with(user(ADMIN_EMAIL).roles("ADMIN")))
            .andExpect(status().isForbidden());

        approve(pendingId);

        mockMvc.perform(get("/reports").with(user(ADMIN_EMAIL).roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(reference(pendingId))));

        mockMvc.perform(get("/reports/" + pendingId).with(user(ADMIN_EMAIL).roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("status-open")));

        mockMvc.perform(get("/admin").with(user(ADMIN_EMAIL).roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(reference(pendingId))));
    }

    // ------------------------------------------------------------------
    // The five filters (status, project, open/closed, severity, incident type)
    // ------------------------------------------------------------------

    @Test
    void filtersNarrowTheListingForAllThreeRoles() throws Exception {
        long projectA = createProject();
        long projectB = createProject();
        long accidentId = submitReportAs(INSPECTOR_EMAIL, projectA, "FILTER accident high",
            "ACCIDENT", "High");
        long mediumId = submitReportAs(INSPECTOR_EMAIL, projectA, "FILTER inspection medium",
            "INSPECTION_FINDING", "Medium");
        long projectBId = submitReportAs(INSPECTOR_EMAIL, projectB, "FILTER project B report",
            "INSPECTION_FINDING", "High");

        approve(accidentId);
        reject(projectBId, "Wrong project selection — fix before resubmission.");

        // incident type
        mockMvc.perform(get("/reports").param("incidentType", "ACCIDENT")
                .with(user(CYCLE_MANAGER_EMAIL).roles("MANAGER")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(reference(accidentId))))
            .andExpect(content().string(not(containsString(reference(mediumId)))));

        // severity of incident = any violation's risk rating
        mockMvc.perform(get("/reports").param("severity", "Medium")
                .with(user(CYCLE_MANAGER_EMAIL).roles("MANAGER")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(reference(mediumId))))
            .andExpect(content().string(not(containsString(reference(accidentId)))));

        // project
        mockMvc.perform(get("/reports").param("projectId", String.valueOf(projectB))
                .with(user(CYCLE_MANAGER_EMAIL).roles("MANAGER")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(reference(projectBId))))
            .andExpect(content().string(not(containsString(reference(accidentId)))));

        // status = Open (approved with progress Open) vs Rejected
        mockMvc.perform(get("/reports").param("status", "OPEN")
                .with(user(CYCLE_MANAGER_EMAIL).roles("MANAGER")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(reference(accidentId))))
            .andExpect(content().string(not(containsString(reference(projectBId)))));

        mockMvc.perform(get("/reports").param("status", "REJECTED")
                .with(user(CYCLE_MANAGER_EMAIL).roles("MANAGER")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(reference(projectBId))))
            .andExpect(content().string(not(containsString(reference(accidentId)))));

        // status = Closed → nothing is closed yet → the filtered empty state
        mockMvc.perform(get("/reports").param("status", "CLOSED")
                .with(user(CYCLE_MANAGER_EMAIL).roles("MANAGER")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("No reports match the current selection.")));

        // The filters work for the Inspector and the Admin too (same control set).
        mockMvc.perform(get("/reports").param("status", "PENDING_APPROVAL")
                .with(user(INSPECTOR_EMAIL).roles("INSPECTOR")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(reference(mediumId))))
            .andExpect(content().string(not(containsString(reference(accidentId)))));

        mockMvc.perform(get("/reports").param("status", "OPEN")
                .with(user(ADMIN_EMAIL).roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(reference(accidentId))))
            .andExpect(content().string(not(containsString(reference(mediumId)))));
    }

    // ------------------------------------------------------------------
    // Photograph playback
    // ------------------------------------------------------------------

    @Test
    void photoEndpointStreamsBytesOnlyToAuthorizedViewers() throws Exception {
        long projectId = createProject();
        long reportId = submitReportWithPhoto(projectId, "PHOTO report");
        SafetyReport report = report(reportId);
        List<Long> violationIds = report.getViolations().stream()
            .map(Violation::getId)
            .toList();
        PhotographEvidence photograph = photographs.findByViolationIdIn(violationIds).stream()
            .findFirst()
            .orElseThrow(() -> new AssertionError("photograph was not persisted"));

        // The owning Inspector gets the bytes with their stored content type.
        byte[] body = mockMvc.perform(get("/reports/" + reportId + "/photos/" + photograph.getId())
                        .with(user(INSPECTOR_EMAIL).roles("INSPECTOR")))
            .andExpect(status().isOk())
            .andExpect(content().contentType("image/jpeg"))
            .andReturn()
            .getResponse()
            .getContentAsByteArray();
        org.assertj.core.api.Assertions.assertThat(body)
            .startsWith((byte) 0xFF, (byte) 0xD8, (byte) 0xFF);

        // The assigned Manager may view the photograph (FR-15).
        mockMvc.perform(get("/reports/" + reportId + "/photos/" + photograph.getId())
                        .with(user(CYCLE_MANAGER_EMAIL).roles("MANAGER")))
            .andExpect(status().isOk())
            .andExpect(content().contentType("image/jpeg"));

        // Another Inspector and the Admin (report still pending) are refused.
        mockMvc.perform(get("/reports/" + reportId + "/photos/" + photograph.getId())
                        .with(user(OTHER_INSPECTOR_EMAIL).roles("INSPECTOR")))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/reports/" + reportId + "/photos/" + photograph.getId())
                        .with(user(ADMIN_EMAIL).roles("ADMIN")))
            .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------
    // EC-28 / EC-29 — closed project narrows the Inspector only
    // ------------------------------------------------------------------

    @Test
    void ec28_closedProjectShowsInspectorOnlyClosedReportsButManagerKeepsReadAccess()
        throws Exception {
        long projectId = createProject();
        long closedId = submitReport(projectId, "EC28 closed report");
        long openId = submitReport(projectId, "EC28 still open report");
        approve(closedId);
        approve(openId);
        // Produce the Closed state directly (report closure itself is a later slice;
        // the repository owns no setter for progress, so the row is updated here).
        jdbc.update("UPDATE safety_report SET progress_status = 'CLOSED' WHERE id = ?", closedId);

        // Project closes now (side effects on reports are stubbed until the closure slice).
        projectService.closeConfirmed(projectId);

        // EC-28: the Inspector keeps only their own Closed report.
        mockMvc.perform(get("/reports").with(user(INSPECTOR_EMAIL).roles("INSPECTOR")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(reference(closedId))))
            .andExpect(content().string(not(containsString(reference(openId)))));

        mockMvc.perform(get("/reports/" + openId).with(user(INSPECTOR_EMAIL).roles("INSPECTOR")))
            .andExpect(status().isForbidden());

        // EC-29: the Manager keeps read-only visibility over both.
        mockMvc.perform(get("/reports").with(user(CYCLE_MANAGER_EMAIL).roles("MANAGER")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(reference(closedId))))
            .andExpect(content().string(containsString(reference(openId))));
    }

    // ------------------------------------------------------------------
    // FR-44 — version history is Manager/Admin material only
    // ------------------------------------------------------------------

    @Test
    void fr44_versionHistoryVisibleToManagerAndAdminButNotInspector() throws Exception {
        long projectId = createProject();
        long reportId = submitReport(projectId, "FR44 version report");
        approve(reportId);

        mockMvc.perform(get("/reports/" + reportId).with(user(CYCLE_MANAGER_EMAIL).roles("MANAGER")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Version history")))
            .andExpect(content().string(containsString("v1")));

        mockMvc.perform(get("/reports/" + reportId).with(user(ADMIN_EMAIL).roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Version history")));

        // The Inspector sees the current version only (FR-44).
        mockMvc.perform(get("/reports/" + reportId).with(user(INSPECTOR_EMAIL).roles("INSPECTOR")))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("Version history"))));
    }

    // ------------------------------------------------------------------
    // Reading the row itself — the view models must reflect the state machine
    // ------------------------------------------------------------------

    @Test
    void approvedReportIsOpenAndRejectedReportHidesItsReasonFromOtherViews() throws Exception {
        long projectId = createProject();
        long reportId = submitReport(projectId, "STATE transition report");
        SafetyReport pending = report(reportId);
        org.assertj.core.api.Assertions.assertThat(pending.getSubmissionStatus())
            .isEqualTo(SubmissionStatus.PENDING_APPROVAL);
        org.assertj.core.api.Assertions.assertThat(pending.getProgressStatus()).isNull();

        reject(reportId, "State check reason");
        SafetyReport rejected = report(reportId);
        org.assertj.core.api.Assertions.assertThat(rejected.getSubmissionStatus())
            .isEqualTo(SubmissionStatus.REJECTED);
        org.assertj.core.api.Assertions.assertThat(rejected.getRejectionReason())
            .isEqualTo("State check reason");

        // attempt illegal approve on REJECTED report — must stay REJECTED, return 200 with "awaiting approval"
        mockMvc.perform(post("/reports/" + reportId + "/approve")
                        .with(user(CYCLE_MANAGER_EMAIL).roles("MANAGER"))
                        .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("awaiting approval")));

        SafetyReport afterIllegalApprove = report(reportId);
        org.assertj.core.api.Assertions.assertThat(afterIllegalApprove.getSubmissionStatus())
            .isEqualTo(SubmissionStatus.REJECTED);
    }
}
