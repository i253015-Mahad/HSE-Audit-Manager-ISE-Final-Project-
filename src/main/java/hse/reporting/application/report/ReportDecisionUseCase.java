package hse.reporting.application.report;

import com.hse.reporting.application.ActorContext;
import com.hse.reporting.projectmanagement.access.ProjectAccessQueries;
import com.hse.reporting.useraccess.access.Actor;
import com.hse.reporting.useraccess.access.AuthorizationDeniedException;
import com.hse.reporting.useraccess.domain.Role;
import hse.reporting.common.BusinessRuleException;
import hse.reporting.notification.NotificationService;
import hse.reporting.report.SafetyReport;
import hse.reporting.report.SafetyReportRepository;
import hse.reporting.report.SafetyReportService;
import hse.reporting.report.SubmissionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Manager decisions on a submitted report (FR-10, FR-11, FR-42, AC-11/AC-12).
 *
 * <p>Actions:
 * <ul>
 *   <li>{@link #approve(Long)} — Pending Approval becomes Open (Admin visibility starts, AC-11)</li>
 *   <li>{@link #reject(Long, String)} — Pending Approval becomes Rejected, the reason is
 *       stored on the report (FR-11) and an in-system notification goes to the OWNING
 *       Inspector only (FR-42, AC-12, AC-41)</li>
 * </ul>
 *
 * <p>Shared guards (denials are decided here, never in the view — ASR-01):
 * <ul>
 *   <li>Actor is a Manager (URL rules additionally require the MANAGER role)</li>
 *   <li>Actor is the <b>currently</b> assigned Manager of the report's project — not the
 *       managerEmail snapshot taken at submission, so reassignment (AC-38) transfers the
 *       permission automatically (BR-04)</li>
 *   <li>Report exists, is not deleted (FR-28) and is Pending Approval (FR-09: no state
 *       change on an Open, Closed or already Rejected report)</li>
 *   <li>Project is still Open (FR-22)</li>
 * </ul>
 */
@Service
public class ReportDecisionUseCase {

    private final SafetyReportRepository reports;
    private final SafetyReportService reportService;
    private final NotificationService notifications;
    private final ProjectAccessQueries projectAccess;
    private final ActorContext actorContext;

    public ReportDecisionUseCase(SafetyReportRepository reports,
                                 SafetyReportService reportService,
                                 NotificationService notifications,
                                 ProjectAccessQueries projectAccess,
                                 ActorContext actorContext) {
        this.reports = reports;
        this.reportService = reportService;
        this.notifications = notifications;
        this.projectAccess = projectAccess;
        this.actorContext = actorContext;
    }

    /** FR-10 / AC-11: Pending Approval → Open. */
    @Transactional
    public void approve(Long reportId) {
        SafetyReport report = requireDecidable(reportId);
        reportService.approve(report);
        reports.saveAndFlush(report);
    }

    /** FR-11 / FR-42 / AC-12: Pending Approval → Rejected + reason + notification. */
    @Transactional
    public void reject(Long reportId, String reason) {
        String trimmed = reason == null ? "" : reason.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessRuleException("Enter a reason for rejecting this report.");
        }
        SafetyReport report = requireDecidable(reportId);
        reportService.reject(report, trimmed);
        reports.saveAndFlush(report);

        // FR-42: in-system notification to the owning Inspector only.
        notifications.record(report.getId(), report.getInspectorEmail(), trimmed);
    }

    /** The report about to be decided, after every shared guard has passed. */
    private SafetyReport requireDecidable(Long reportId) {
        Actor actor = actorContext.currentActor();
        if (actor.role() != Role.MANAGER) {
            throw new AuthorizationDeniedException("Only the project's Manager can approve or reject reports.");
        }

        SafetyReport report = reports.findByIdAndDeletedFalse(reportId)
            .orElseThrow(() -> new AuthorizationDeniedException(
                "This report does not exist or has been deleted."));

        // FR-09: only a report awaiting approval may change state.
        if (report.getSubmissionStatus() != SubmissionStatus.PENDING_APPROVAL) {
            throw new BusinessRuleException(
                "Only a report awaiting approval can be approved or rejected; this report is "
                    + report.getSubmissionStatus().name().toLowerCase().replace('_', ' ') + ".");
        }

        // BR-04 / AC-38: current assignment decides, never the submission-time snapshot.
        if (!projectAccess.isAssignedManager(actor.email(), report.getProjectId())) {
            throw new AuthorizationDeniedException(
                "You are not the Manager of the project this report belongs to.");
        }

        // FR-22: no lifecycle change once the project is closed.
        Optional<ProjectAccessQueries.ProjectSummary> project =
            projectAccess.projectsByIds(List.of(report.getProjectId())).stream().findFirst();
        if (project.isPresent() && !project.get().open()) {
            throw new BusinessRuleException(
                "The project is closed, so its reports can no longer be approved or rejected.");
        }

        return report;
    }
}
