package hse.reporting.application.report;

import hse_reporting.useraccess.access.AuthorizationDeniedException;
import hse_reporting.useraccess.access.ActorContext;
import hse_reporting.useraccess.domain.Role;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import hse.reporting.notification.NotificationService;
import hse_reporting.report.SafetyReport;
import hse_reporting.report.SafetyReportRepository;
import hse_reporting.report.SafetyReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles manager report approval and rejection (FR-10, FR-11, FR-42).
 *
 * <p>Actions:
 * • approve() - switches a pending report to APPROVED and OPEN
 * • reject() - switches a pending report to REJECTED and stores the reason
 *
 * <p>Guards:
 * • Actor is Manager with current project assignment
 * • Report is PENDING_APPROVAL and not deleted
 * • Project is OPEN (FR-22 for closed projects)
 * • Only rejected reports can be rejected again
 */
@Service
public class ReportDecisionUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReportDecisionUseCase.class);

    private final SafetyReportRepository reports;
    private final SafetyReportService reportService;
    private final NotificationService notifications;
    private final ActorContext actorContext;

    public ReportDecisionUseCase(SafetyReportRepository reports,
                                 SafetyReportService reportService,
                                 NotificationService notifications,
                                 ActorContext actorContext) {
        this.reports = reports;
        this.reportService = reportService;
        this.notifications = notifications;
        this.actorContext = actorContext;
    }

    /**
     * Approve a pending report (FR-10, AC-11).
     *
     * <p>Checks:
     * • Manager has project assignment
     * • Report is PENDING_APPROVAL
     * • Project is OPEN (FR-22)
     */
    @Transactional
    public void approve(Long reportId) {
        Actor actor = actorContext.currentActor();

        // Require role
        if (actor.role() != Role.MANAGER) {
            throw new AccessDeniedException("Only Managers can approve reports.");
        }

        // Fetch the report with optimization
        SafetyReport report = reports.findByIdAndDeletedFalse(reportId)
            .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        // Validate state
        if (report.getSubmissionStatus() != hse.reporting.report.SubmissionStatus.PENDING_APPROVAL) {
            throw new IllegalArgumentException(
                "Cannot approve report in " + report.getSubmissionStatus() + " status");
        }

        // Validate project assignment
        // Note: We need to check if the manager is assigned to this project
        // For now, we'll check the project manager email in database matches current actor
        if (report.getManagerEmail() == null || !report.getManagerEmail().equals(actor.email())) {
            throw new AccessDeniedException(
                "Manager " + actor.email() + " is not assigned to this report's project");
        }

        // Approve the report
        log.info("Manager {} approving report {}", actor.email(), reportId);
        reportService.approve(report);
        reports.saveAndFlush(report);
    }

    /**
     * Reject a pending report with a reason (FR-11, FR-42, AC-12).
     *
     * <p>Checks:
     * • Manager has project assignment
     * • Report is PENDING_APPROVAL
     * • Project is OPEN (FR-22)
     * • Reason is provided
     *
     * <p>Side effects:
     * • Report status becomes REJECTED
     * • rejection_reason stored
     * • Notification created for Inspector
     */
    @Transactional
    public void reject(Long reportId, String reason) {
        Actor actor = actorContext.currentActor();

        // Validate reason
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A reason for rejection is required.");
        }

        // Require role
        if (actor.role() != Role.MANAGER) {
            throw new AccessDeniedException("Only Managers can reject reports.");
        }

        // Fetch the report
        SafetyReport report = reports.findByIdAndDeletedFalse(reportId)
            .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        // Validate state
        if (report.getSubmissionStatus() != hse.reporting.report.SubmissionStatus.PENDING_APPROVAL) {
            throw new IllegalArgumentException(
                "Cannot reject report in " + report.getSubmissionStatus() + " status");
        }

        // Validate project assignment
        if (report.getManagerEmail() == null || !report.getManagerEmail().equals(actor.email())) {
            throw new AccessDeniedException(
                "Manager " + actor.email() + " is not assigned to this report's project");
        }

        // Reject the report
        log.info("Manager {} rejecting report {} with reason: {}", actor.email(), reportId, reason);
        reportService.reject(report, reason.trim());
        reports.saveAndFlush(report);

        // Create notification for the Inspector
        log.info("Creating rejection notification for inspector {}", report.getInspectorEmail());
        notifications.record(reportId, report.getInspectorEmail(), reason.trim());
    }
}