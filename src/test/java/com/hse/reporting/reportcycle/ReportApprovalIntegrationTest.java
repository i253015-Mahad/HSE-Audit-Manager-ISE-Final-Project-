package com.hse.reporting.reportcycle;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import hse.reporting.report.ProgressStatus;
import hse.reporting.report.SubmissionStatus;

/**
 * Slice 4 approval cycle (FR-10/FR-11/FR-42/FR-43/FR-44, BR-04, FR-22,
 * AC-11/AC-12/AC-37): approve → Open, reject → reason + notification,
 * edit & resubmit → Pending Approval with a new version.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportApprovalIntegrationTest extends ReportCycleTestSupport {

    private static final String REJECTION_REASON =
        "Missing permit — fix before resubmission " + UUID.randomUUID().toString().substring(0, 6);

    // ------------------------------------------------------------------
    // FR-10 / AC-11 — approve moves the report to Open
    // ------------------------------------------------------------------

    @Test
    void ac11_approveMovesReportToOpenAndItStaysOnManagerAndAdminLists() throws Exception {
        long projectId = createProject();
        long reportId = submitReport(projectId, "AC11 approve me");

        // The Manager gets the decision UI on the detail page (FR-10).
        mockMvc.perform(get("/reports/" + reportId).with(user(CYCLE_MANAGER_EMAIL).roles("MANAGER")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Manager decision")))
            .andExpect(content().string(containsString("Approve — move to Open")));

        approve(reportId);

        // Row state: APPROVED + OPEN (FR-10, AC-11).
        org.assertj.core.api.Assertions.assertThat(report(reportId).getSubmissionStatus())
            .isEqualTo(SubmissionStatus.APPROVED);
        org.assertj.core.api.Assertions.assertThat(report(reportId).getProgressStatus())
            .isEqualTo(ProgressStatus.OPEN);

        // AC-15: the approved report STAYS on the Manager's list — badge flips to Open.
        mockMvc.perform(get("/reports").with(user(CYCLE_MANAGER_EMAIL).roles("MANAGER")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(reference(reportId))))
            .andExpect(content().string(containsString("status-open")));

        mockMvc.perform(get("/reports/" + reportId).with(user(CYCLE_MANAGER_EMAIL).roles("MANAGER")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("status-open")))
            // no further decisions once Open (FR-09)
            .andExpect(content().string(not(containsString("Manager decision"))));

        // FR-16 / AC-11: the Admin sees it now, on list and dashboard.
        mockMvc.perform(get("/reports").with(user(ADMIN_EMAIL).roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(reference(reportId))));

        mockMvc.perform(get("/admin").with(user(ADMIN_EMAIL).roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(reference(reportId))));
    }

    // ------------------------------------------------------------------
    // FR-11 / FR-42 / AC-12 — reject stores the reason and notifies the owner
    // ------------------------------------------------------------------

    @Test
    void ac12_rejectStoresReasonNotifiesOwningInspectorAndBlocksFurtherDecisions()
        throws Exception {
        long projectId = createProject();
        long reportId = submitReport(projectId, "AC12 reject me");

        reject(reportId, REJECTION_REASON);

        // Row state: REJECTED + reason, never opened (AC-12: cannot open/close a rejection).
        org.assertj.core.api.Assertions.assertThat(report(reportId).getSubmissionStatus())
            .isEqualTo(SubmissionStatus.REJECTED);
        org.assertj.core.api.Assertions.assertThat(report(reportId).getRejectionReason())
            .isEqualTo(REJECTION_REASON);
        org.assertj.core.api.Assertions.assertThat(report(reportId).getProgressStatus()).isNull();

        // FR-42: exactly one in-system notification, for the OWNING Inspector.
        org.assertj.core.api.Assertions.assertThat(notificationCount(reportId)).isEqualTo(1);
        Integer forOwner = jdbc.queryForObject(
            "SELECT COUNT(*) FROM notification WHERE report_id = ? AND inspector_email = ?",
            Integer.class, reportId, INSPECTOR_EMAIL);
        org.assertj.core.api.Assertions.assertThat(forOwner).isEqualTo(1);

        // The Inspector's dashboard carries the reason (FR-42).
        mockMvc.perform(get("/inspector").with(user(INSPECTOR_EMAIL).roles("INSPECTOR")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(reference(reportId))))
            .andExpect(content().string(containsString(REJECTION_REASON)));

        // Nobody else sees the notification (FR-42 / AC-41).
        mockMvc.perform(get("/manager").with(user(CYCLE_MANAGER_EMAIL).roles("MANAGER")))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString(REJECTION_REASON))));

        mockMvc.perform(get("/admin").with(user(ADMIN_EMAIL).roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString(REJECTION_REASON))));

        // FR-09: a rejected report cannot be decided again — the attempt is refused
        // on the detail page and the row keeps its state (AC-12).
        mockMvc.perform(post("/reports/" + reportId + "/approve")
                        .with(user(CYCLE_MANAGER_EMAIL).roles("MANAGER"))
                        .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("awaiting approval")));

        org.assertj.core.api.Assertions.assertThat(report(reportId).getSubmissionStatus())
            .isEqualTo(SubmissionStatus.REJECTED);
        org.assertj.core.api.Assertions.assertThat(report(reportId).getProgressStatus()).isNull();
    }

    // ------------------------------------------------------------------
    // FR-11 — a rejection reason is mandatory
    // ------------------------------------------------------------------

    @Test
    void fr11_rejectWithoutReasonIsRefusedAndReportStaysPending() throws Exception {
        long projectId = createProject();
        long reportId = submitReport(projectId, "FR11 needs a reason");

        mockMvc.perform(post("/reports/" + reportId + "/reject")
                        .with(user(CYCLE_MANAGER_EMAIL).roles("MANAGER"))
                        .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Enter a reason")));

        org.assertj.core.api.Assertions.assertThat(report(reportId).getSubmissionStatus())
            .isEqualTo(SubmissionStatus.PENDING_APPROVAL);
        org.assertj.core.api.Assertions.assertThat(notificationCount(reportId)).isEqualTo(0);
    }

    // ------------------------------------------------------------------
    // BR-04 / AC-13 — only the currently assigned Manager decides
    // ------------------------------------------------------------------

    @Test
    void br04_onlyTheAssignedManagerCanApproveOrReject() throws Exception {
        long projectId = createProject();
        long reportId = submitReport(projectId, "BR04 guarded report");

        // Another Manager (right role, wrong project) → 403.
        mockMvc.perform(post("/reports/" + reportId + "/approve")
                        .with(user(OTHER_MANAGER_EMAIL).roles("MANAGER"))
                        .with(csrf()))
            .andExpect(status().isForbidden());

        // The Admin and the Inspector never reach the decision endpoint (URL rules).
        mockMvc.perform(post("/reports/" + reportId + "/approve")
                        .with(user(ADMIN_EMAIL).roles("ADMIN"))
                        .with(csrf()))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/reports/" + reportId + "/reject")
                        .with(user(INSPECTOR_EMAIL).roles("INSPECTOR"))
                        .with(csrf())
                        .param("rejectReason", "not mine to reject"))
            .andExpect(status().isForbidden());

        org.assertj.core.api.Assertions.assertThat(report(reportId).getSubmissionStatus())
            .isEqualTo(SubmissionStatus.PENDING_APPROVAL);
        org.assertj.core.api.Assertions.assertThat(notificationCount(reportId)).isEqualTo(0);
    }

    // ------------------------------------------------------------------
    // FR-22 — a closed project freezes the lifecycle
    // ------------------------------------------------------------------

    @Test
    void fr22_closedProjectBlocksApproveAndReject() throws Exception {
        long projectId = createProject();
        long reportId = submitReport(projectId, "FR22 frozen report");
        projectService.closeConfirmed(projectId);

        mockMvc.perform(post("/reports/" + reportId + "/approve")
                        .with(user(CYCLE_MANAGER_EMAIL).roles("MANAGER"))
                        .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("project is closed")));

        mockMvc.perform(post("/reports/" + reportId + "/reject")
                        .with(user(CYCLE_MANAGER_EMAIL).roles("MANAGER"))
                        .with(csrf())
                        .param("rejectReason", "cannot reject into a closed project"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("project is closed")));

        org.assertj.core.api.Assertions.assertThat(report(reportId).getSubmissionStatus())
            .isEqualTo(SubmissionStatus.PENDING_APPROVAL);
        org.assertj.core.api.Assertions.assertThat(notificationCount(reportId)).isEqualTo(0);
    }

    // ------------------------------------------------------------------
    // FR-43 / AC-37 — edit & resubmit of a rejected report
    // ------------------------------------------------------------------

    @Test
    void ac37_fr43_editAndResubmitReturnsTheSameReportToPendingApproval() throws Exception {
        long projectId = createProject();
        long reportId = submitReport(projectId, "AC37 original location");
        reject(reportId, REJECTION_REASON);

        // Only the owning Inspector may open the edit form (FR-43).
        mockMvc.perform(get("/reports/" + reportId + "/edit")
                        .with(user(OTHER_INSPECTOR_EMAIL).roles("INSPECTOR")))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/reports/" + reportId + "/edit")
                        .with(user(CYCLE_MANAGER_EMAIL).roles("MANAGER")))
            .andExpect(status().isForbidden());

        // The owner gets the pre-filled form with the rejection banner.
        String editForm = mockMvc.perform(get("/reports/" + reportId + "/edit")
                        .with(user(INSPECTOR_EMAIL).roles("INSPECTOR")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Edit Report")))
            .andExpect(content().string(containsString("Resubmit rejected report")))
            .andExpect(content().string(containsString(REJECTION_REASON)))
            .andExpect(content().string(containsString("AC37 original location")))
            .andReturn()
            .getResponse()
            .getContentAsString();
        org.assertj.core.api.Assertions.assertThat(editForm)
            .contains("/reports/" + reportId + "/resubmit")
            .doesNotContain("formError");

        // Resubmit with corrected content → back to Pending Approval, SAME row (FR-43).
        String newLocation = "AC37 corrected location";
        mockMvc.perform(post("/reports/" + reportId + "/resubmit")
                        .with(user(INSPECTOR_EMAIL).roles("INSPECTOR"))
                        .with(csrf())
                        .param("projectId", String.valueOf(projectId))
                        .param("observationDate", "2026-11-28")
                        .param("observationTime", "11:15")
                        .param("location", newLocation)
                        .param("observationType", "INSPECTION_FINDING")
                        .param("violations[0].briefDescription", "Corrected edge description")
                        .param("violations[0].observationHazard", "Unsafe condition")
                        .param("violations[0].riskRating", "High")
                        .param("violations[0].actionRecommended", "Install guardrails")
                        .param("violations[0].violatorName", "Cycle Violator")
                        .param("violations[0].violatorBadgeNo", "B-777")
                        .param("violations[0].violatorTitle", "Labourer")
                        .param("violations[0].taskIncharge", "Cycle Foreman"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/reports/submitted"));

        // Same id, PENDING_APPROVAL again, new content, reason retained (displayed
        // only while Rejected — the value itself survives the resubmission).
        var resubmitted = report(reportId);
        org.assertj.core.api.Assertions.assertThat(resubmitted.getSubmissionStatus())
            .isEqualTo(SubmissionStatus.PENDING_APPROVAL);
        org.assertj.core.api.Assertions.assertThat(resubmitted.getProgressStatus()).isNull();
        org.assertj.core.api.Assertions.assertThat(resubmitted.getLocation())
            .isEqualTo(newLocation);
        org.assertj.core.api.Assertions.assertThat(resubmitted.getRejectionReason())
            .isEqualTo(REJECTION_REASON);
        org.assertj.core.api.Assertions.assertThat(resubmitted.getInspectorEmail())
            .isEqualTo(INSPECTOR_EMAIL);

        // FR-44: version 2 was snapshotted (v1 = original submission, v2 = resubmission).
        org.assertj.core.api.Assertions.assertThat(versionCount(reportId)).isEqualTo(2);

        // FR-43: a resubmitted (Pending) report is no longer editable.
        mockMvc.perform(get("/reports/" + reportId + "/edit")
                        .with(user(INSPECTOR_EMAIL).roles("INSPECTOR")))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/reports/" + reportId));

        mockMvc.perform(post("/reports/" + reportId + "/resubmit")
                        .with(user(INSPECTOR_EMAIL).roles("INSPECTOR"))
                        .with(csrf())
                        .param("projectId", String.valueOf(projectId))
                        .param("observationDate", "2026-11-28")
                        .param("observationTime", "11:15")
                        .param("location", newLocation)
                        .param("observationType", "INSPECTION_FINDING")
                        .param("violations[0].briefDescription", "Corrected edge description")
                        .param("violations[0].observationHazard", "Unsafe condition")
                        .param("violations[0].riskRating", "High")
                        .param("violations[0].actionRecommended", "Install guardrails")
                        .param("violations[0].violatorName", "Cycle Violator")
                        .param("violations[0].violatorBadgeNo", "B-777")
                        .param("violations[0].violatorTitle", "Labourer")
                        .param("violations[0].taskIncharge", "Cycle Foreman"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/reports/" + reportId));
    }

    // ------------------------------------------------------------------
    // FR-06 — the duplicate check excludes the report being resubmitted
    // ------------------------------------------------------------------

    @Test
    void fr06_duplicateCheckExcludesTheReportBeingResubmitted() throws Exception {
        long projectId = createProject();
        String location = "FR06 unique " + UUID.randomUUID().toString().substring(0, 8);
        long reportId = submitReport(projectId, location);
        reject(reportId, REJECTION_REASON);

        // Resubmit with byte-identical content: only itself could match, and FR-06
        // excludes self — so this must succeed instead of failing as a duplicate.
        mockMvc.perform(post("/reports/" + reportId + "/resubmit")
                        .with(user(INSPECTOR_EMAIL).roles("INSPECTOR"))
                        .with(csrf())
                        .param("projectId", String.valueOf(projectId))
                        .param("observationDate", "2026-11-27")
                        .param("observationTime", "09:30")
                        .param("location", location)
                        .param("observationType", "INSPECTION_FINDING")
                        .param("violations[0].briefDescription", "Unprotected edge " + location)
                        .param("violations[0].observationHazard", "Unsafe condition")
                        .param("violations[0].riskRating", "High")
                        .param("violations[0].actionRecommended", "Install guardrails")
                        .param("violations[0].violatorName", "Cycle Violator")
                        .param("violations[0].violatorBadgeNo", "B-777")
                        .param("violations[0].violatorTitle", "Labourer")
                        .param("violations[0].taskIncharge", "Cycle Foreman"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/reports/submitted"));

        org.assertj.core.api.Assertions.assertThat(report(reportId).getSubmissionStatus())
            .isEqualTo(SubmissionStatus.PENDING_APPROVAL);
    }

    // ------------------------------------------------------------------
    // FR-43 — Open/Closed/Pending reports are not editable
    // ------------------------------------------------------------------

    @Test
    void fr43_onlyRejectedReportsAreEditable() throws Exception {
        long projectId = createProject();
        long reportId = submitReport(projectId, "FR43 pending report");

        // Pending: edit form refused with a redirect to the detail page.
        mockMvc.perform(get("/reports/" + reportId + "/edit")
                        .with(user(INSPECTOR_EMAIL).roles("INSPECTOR")))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/reports/" + reportId));

        // attempt illegal approve on REJECTED report — must stay REJECTED, return 200 with "awaiting approval"
        mockMvc.perform(post("/reports/" + reportId + "/approve")
                        .with(user(INSPECTOR_EMAIL).roles("INSPECTOR"))
                        .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("awaiting approval")));

        // Open: same refusal (edit form refused, no edit/resubmit on detail).
        mockMvc.perform(get("/reports/" + reportId + "/edit")
                        .with(user(INSPECTOR_EMAIL).roles("INSPECTOR")))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/reports/" + reportId));

        // The detail page for an Open report offers no edit/resubmit entry point.
        mockMvc.perform(get("/reports/" + reportId).with(user(INSPECTOR_EMAIL).roles("INSPECTOR")))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("Edit & resubmit"))));
    }
}
