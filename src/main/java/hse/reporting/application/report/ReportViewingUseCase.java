package hse.reporting.application.report;

import com.hse.reporting.application.ActorContext;
import com.hse.reporting.useraccess.access.Actor;
import com.hse.reporting.useraccess.access.AuthorizationDeniedException;
import com.hse.reporting.useraccess.domain.Role;
import com.hse.reporting.projectmanagement.access.ProjectAccessQueries;
import hse.reporting.photo.PhotographEvidence;
import hse.reporting.photo.PhotoEvidenceQuery;
import hse.reporting.report.SafetyReport;
import hse.reporting.report.SafetyReportRepository;
import hse.reporting.report.SafetyReportService;
import hse.reporting.report.SubmissionStatus;
import hse.reporting.report.ReportVersion;
import hse.reporting.notification.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.*;
import java.util.stream.Collectors;

/** Coordinates role‑scoped report viewing (FR‑14/15/16), detail retrieval,
 *  photo playback, version history and rejection notifications (FR‑42). */
@Service
public class ReportViewingUseCase {

    private final SafetyReportRepository reports;
    private final SafetyReportService reportService;
    private final ProjectAccessQueries projectAccess;
    private final PhotoEvidenceQuery photos;
    private final NotificationService notifications;
    private final ActorContext actorContext;

    public ReportViewingUseCase(SafetyReportRepository reports,
                                SafetyReportService reportService,
                                ProjectAccessQueries projectAccess,
                                PhotoEvidenceQuery photos,
                                NotificationService notifications,
                                ActorContext actorContext) {
        this.reports = reports;
        this.reportService = reportService;
        this.projectAccess = projectAccess;
        this.photos = photos;
        this.notifications = notifications;
        this.actorContext = actorContext;
    }

    /** The currently authenticated actor, never null (throws if anonymous). */
    private Actor actor() {
        return actorContext.currentActor();
    }

    /** Role for the current actor, never null. */
    private Role role() {
        return actor().role();
    }

    // -----------------------------------------------------------------
    // Role‑scoped lists (FR‑14 / FR‑15 / FR‑16)
    // -----------------------------------------------------------------

    /** FR‑14 / AC‑14: Inspector sees only their own non‑deleted reports. */
    @Transactional(readOnly = true)
    public List<SafetyReport> listForInspector() {
        return visibleToInspector(actor().email());
    }

    /** FR‑15 / AC‑15: Manager sees reports of projects they handle. */
    @Transactional(readOnly = true)
    public List<SafetyReport> listForManager() {
        Actor a = actor();
        Collection<Long> projectIds = projectAccess.projectsAssignedTo(a.email()).stream()
                .map(p -> p.id()).collect(Collectors.toList());
        return visibleToProjects(projectIds);
    }

    /** FR‑16 / AC‑16: Admin sees approved, non‑deleted reports across all projects. */
    @Transactional(readOnly = true)
    public List<SafetyReport> listForAdmin() {
        return approvedNonDeleted();
    }

    /** Convenient combination for the presentation layer. */
    public enum Scope { INSPECTOR, MANAGER, ADMIN }

    public Scope currentScope() {
        Role r = role();
        if (r == Role.INSPECTOR) return Scope.INSPECTOR;
        if (r == Role.MANAGER) return Scope.MANAGER;
        return Scope.ADMIN;
    }

    // -----------------------------------------------------------------
    // Detail view (report page + photos + version history)
    // -----------------------------------------------------------------

    /** FR‑14/15/16: detail for the authorised actor; throws if invisible. */
    @Transactional(readOnly = true)
    public SafetyReport detail(Long id) {
        Actor a = actor();
        Role r = a.role();

        SafetyReport report;
        if (r == Role.INSPECTOR) {
            report = reports.findByIdAndInspectorEmailAndDeletedFalse(id, a.email())
                    .orElseThrow(() -> new AuthorizationDeniedException(
                            "Inspector cannot view report " + id));
        } else if (r == Role.MANAGER) {
            Collection<Long> projectIds = projectAccess.projectsAssignedTo(a.email()).stream()
                    .map(p -> p.id()).collect(Collectors.toList());
            Optional<SafetyReport> maybe = reports.findByIdAndProjectIdInAndDeletedFalse(id, projectIds);
            if (maybe.isEmpty()) {
                throw new AuthorizationDeniedException(
                        "Manager cannot view report " + id + " outside assigned projects");
            }
            report = maybe.get();
        } else { // ADMIN
            report = reports.findByIdAndSubmissionStatusAndDeletedFalse(id, SubmissionStatus.APPROVED)
                    .orElseThrow(() -> new AuthorizationDeniedException(
                            "Admin cannot view pending/rejected report " + id));
        }
        return report;
    }

    /** Photos attached to the given report's violations. */
    @Transactional(readOnly = true)
    public List<PhotographEvidence> photosOfReport(Long reportId) {
        SafetyReport report = detail(reportId); // ensures authorisation first
        return photos.forViolations(report.getViolations().stream()
                .map(v -> v.getId()).collect(Collectors.toSet()));
    }

    /** One photograph, owned by one of the report's violations. */
    @Transactional(readOnly = true)
    public Optional<PhotographEvidence> photoOwnedBy(Long photoId, Long reportId) {
        SafetyReport report = detail(reportId);
        return photos.findOwnedBy(photoId, report.getViolations().stream()
                .map(v -> v.getId()).collect(Collectors.toSet()));
    }

    /** Version snapshots for the detail timeline. */
    @Transactional(readOnly = true)
    public List<ReportVersion> versionsOf(Long reportId) {
        return reportService.versionsOf(reportId);
    }

    // -----------------------------------------------------------------
    // Rejection notifications (FR‑42)
    // -----------------------------------------------------------------

    /** All notifications for the current actor (Inspector only). */
    @Transactional(readOnly = true)
    public List<hse.reporting.notification.Notification> myNotifications() {
        Actor a = actor();
        if (a.role() != Role.INSPECTOR) {
            throw new AuthorizationDeniedException(
                    "Only Inspectors have rejection notifications");
        }
        return notifications.forInspector(a.email());
    }

    // -----------------------------------------------------------------
    // Internal role‑scoped queries (mirror SafetyReportService facades)
    // -----------------------------------------------------------------

    private List<SafetyReport> visibleToInspector(String inspectorEmail) {
        return reports.findByInspectorEmailAndDeletedFalseOrderBySubmittedAtDesc(inspectorEmail);
    }

    private Optional<SafetyReport> visibleToInspector(Long id, String inspectorEmail) {
        return reports.findByIdAndInspectorEmailAndDeletedFalse(id, inspectorEmail);
    }

    private List<SafetyReport> visibleToProjects(Collection<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return List.of();
        }
        return reports.findByProjectIdInAndDeletedFalseOrderBySubmittedAtDesc(projectIds);
    }

    private Optional<SafetyReport> visibleToProjects(Long id, Collection<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Optional.empty();
        }
        return reports.findByIdAndProjectIdInAndDeletedFalse(id, projectIds);
    }

    private List<SafetyReport> approvedNonDeleted() {
        return reports.findBySubmissionStatusAndDeletedFalseOrderBySubmittedAtDesc(SubmissionStatus.APPROVED);
    }

    private Optional<SafetyReport> approvedNonDeleted(Long id) {
        return reports.findByIdAndSubmissionStatusAndDeletedFalse(id, SubmissionStatus.APPROVED);
    }
}