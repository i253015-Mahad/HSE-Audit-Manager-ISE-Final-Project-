package hse.reporting.application.report;

import com.hse.reporting.application.ActorContext;
import com.hse.reporting.projectmanagement.access.ProjectAccessQueries;
import com.hse.reporting.projectmanagement.access.ProjectAccessQueries.ProjectSummary;
import com.hse.reporting.useraccess.access.Actor;
import com.hse.reporting.useraccess.access.AuthorizationDeniedException;
import com.hse.reporting.useraccess.domain.Role;
import hse.reporting.common.BusinessRuleException;
import hse.reporting.notification.NotificationService;
import hse.reporting.photo.PhotoEvidenceQuery;
import hse.reporting.photo.PhotographEvidence;
import hse.reporting.report.ProgressStatus;
import hse.reporting.report.SafetyReport;
import hse.reporting.report.SafetyReportRepository;
import hse.reporting.report.SafetyReportService;
import hse.reporting.report.SubmissionStatus;
import hse.reporting.report.Violation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Role-scoped report viewing (FR-14/15/16), detail retrieval, photograph playback,
 * version history (FR-44) and rejection notifications (FR-42).
 *
 * <p>Every visibility decision happens here — the presentation layer only renders
 * what this use case returns (AGENTS.md §8 / ASR-01). A filter can only narrow
 * the role-scoped set, never widen it.
 */
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

    private Role role() {
        return actor().role();
    }

    public enum Scope { INSPECTOR, MANAGER, ADMIN }

    public Scope currentScope() {
        return switch (role()) {
            case INSPECTOR -> Scope.INSPECTOR;
            case MANAGER -> Scope.MANAGER;
            case ADMIN -> Scope.ADMIN;
        };
    }

    /** Display reference {@code RPT-%04d} — derived in the view model, never stored. */
    public static String reference(Long reportId) {
        return String.format("RPT-%04d", reportId);
    }

    /** Project name for the detail page — looked up through the approved port. */
    @Transactional(readOnly = true)
    public String projectName(Long projectId) {
        return projectAccess.projectsByIds(List.of(projectId)).stream()
            .map(ProjectSummary::name)
            .findFirst()
            .orElse("—");
    }

    // -----------------------------------------------------------------
    // Listing with filters (FR-14/15/16)
    // -----------------------------------------------------------------

    /**
     * The actor's own visibility set before any user-selected filter —
     * FR-14 own reports, FR-15 assigned projects, FR-16 approved only (FR-12).
     */
    private List<SafetyReport> roleScoped() {
        Actor a = actor();
        return switch (a.role()) {
            case INSPECTOR -> reports.findByInspectorEmailAndDeletedFalseOrderBySubmittedAtDesc(a.email());
            case MANAGER -> visibleToProjects(assignedProjectIds(a));
            case ADMIN -> reports.findBySubmissionStatusAndDeletedFalseOrderBySubmittedAtDesc(
                SubmissionStatus.APPROVED);
        };
    }

    /**
     * Listing for the report table. Order: role scope (FR-14/15/16) →
     * EC-28 narrowing for Inspectors → the user's filters.
     */
    @Transactional(readOnly = true)
    public List<ReportListItem> list(ReportFilter filter) {
        ReportFilter effective = filter == null ? ReportFilter.NONE : filter;
        List<SafetyReport> scoped = roleScoped();
        if (role() == Role.INSPECTOR) {
            scoped = applyEc28(scoped);
        }
        Map<Long, String> projectNames = projectNameById(scoped);
        return applyFilters(scoped, effective).stream()
            .map(report -> toListItem(report, projectNames))
            .toList();
    }

    private ReportListItem toListItem(SafetyReport report, Map<Long, String> projectNames) {
        String status = statusLabel(report);
        return new ReportListItem(
            report.getId(),
            reference(report.getId()),
            projectNames.getOrDefault(report.getProjectId(), "—"),
            report.getObservationDate(),
            report.getLocation(),
            status,
            ReportListItem.deriveStatusClass(status),
            rejectionReasonFor(report));
    }

    /**
     * EC-28: after project closure an Inspector sees only their own Closed
     * reports. Managers and Admins keep read-only visibility (EC-29).
     */
    private List<SafetyReport> applyEc28(List<SafetyReport> scoped) {
        if (scoped.isEmpty()) {
            return scoped;
        }
        Map<Long, ProjectSummary> projects = projectById(scoped);
        return scoped.stream()
            .filter(report -> {
                ProjectSummary project = projects.get(report.getProjectId());
                return project == null || project.open()
                    || report.getProgressStatus() == ProgressStatus.CLOSED;
            })
            .toList();
    }

    private List<SafetyReport> applyFilters(List<SafetyReport> base, ReportFilter filter) {
        Stream<SafetyReport> stream = base.stream();
        if (filter.hasStatus()) {
            stream = stream.filter(report -> matchesStatus(report, filter.status()));
        }
        if (filter.projectId() != null) {
            stream = stream.filter(report -> filter.projectId().equals(report.getProjectId()));
        }
        if (filter.incidentType() != null && !filter.incidentType().isBlank()) {
            String wanted = filter.incidentType().trim().toUpperCase();
            stream = stream.filter(report -> report.getObservationType() != null
                && wanted.equals(report.getObservationType().name()));
        }
        if (filter.severity() != null && !filter.severity().isBlank()) {
            String wanted = filter.severity().trim();
            // Severity of incident: the report matches when ANY violation carries the rating.
            stream = stream.filter(report -> report.getViolations().stream()
                .anyMatch(violation -> wanted.equalsIgnoreCase(violation.getRiskRating())
                    || wanted.equalsIgnoreCase(violation.getRiskRatingOther())));
        }
        return stream.toList();
    }

    /** Status filter: Open/Closed mean APPROVED reports with that progress (FR-09). */
    private boolean matchesStatus(SafetyReport report, String status) {
        return switch (status.trim().toUpperCase()) {
            case "PENDING_APPROVAL", "PENDING" ->
                report.getSubmissionStatus() == SubmissionStatus.PENDING_APPROVAL;
            case "REJECTED", "REJECT" ->
                report.getSubmissionStatus() == SubmissionStatus.REJECTED;
            case "OPEN" -> report.getSubmissionStatus() == SubmissionStatus.APPROVED
                && report.getProgressStatus() == ProgressStatus.OPEN;
            case "CLOSED" -> report.getSubmissionStatus() == SubmissionStatus.APPROVED
                && report.getProgressStatus() == ProgressStatus.CLOSED;
            default -> true; // ALL or unknown → no narrowing
        };
    }

    private String statusLabel(SafetyReport report) {
        if (report.getSubmissionStatus() == SubmissionStatus.APPROVED) {
            return report.getProgressStatus() == ProgressStatus.CLOSED ? "Closed" : "Open";
        }
        if (report.getSubmissionStatus() == SubmissionStatus.REJECTED) {
            return "Rejected";
        }
        return "Pending Approval";
    }

    /** FR-11 keeps the reason for history; it is displayed only while Rejected. */
    private String rejectionReasonFor(SafetyReport report) {
        return report.getSubmissionStatus() == SubmissionStatus.REJECTED
            ? report.getRejectionReason() : null;
    }

    // -----------------------------------------------------------------
    // Filter dropdown sources — always the UNFILTERED role-scoped set, so
    // picking a status never collapses the project list.
    // -----------------------------------------------------------------

    /** Role-aware status dropdown: Admin is limited to All / Open / Closed (FR-12). */
    public List<StatusOption> statusOptions() {
        if (role() == Role.ADMIN) {
            return List.of(
                new StatusOption("ALL", "All"),
                new StatusOption("OPEN", "Open"),
                new StatusOption("CLOSED", "Closed"));
        }
        return List.of(
            new StatusOption("ALL", "All"),
            new StatusOption("PENDING_APPROVAL", "Pending Approval"),
            new StatusOption("REJECTED", "Rejected"),
            new StatusOption("OPEN", "Open"),
            new StatusOption("CLOSED", "Closed"));
    }

    public record StatusOption(String value, String label) {
    }

    @Transactional(readOnly = true)
    public List<ProjectSummary> projectOptions() {
        Actor a = actor();
        return switch (a.role()) {
            case MANAGER -> projectAccess.projectsAssignedTo(a.email());
            case ADMIN -> projectAccess.allProjects();
            case INSPECTOR -> projectAccess.projectsByIds(
                reports.findByInspectorEmailAndDeletedFalseOrderBySubmittedAtDesc(a.email()).stream()
                    .map(SafetyReport::getProjectId)
                    .collect(Collectors.toSet()));
        };
    }

    // -----------------------------------------------------------------
    // Detail view (report page + photos + version history)
    // -----------------------------------------------------------------

    /** FR-14/15/16: detail for the authorised actor; throws 403 if invisible. */
    @Transactional(readOnly = true)
    public SafetyReport detail(Long id) {
        Actor a = actor();
        SafetyReport report = switch (a.role()) {
            case INSPECTOR -> reports.findByIdAndInspectorEmailAndDeletedFalse(id, a.email())
                .orElseThrow(() -> new AuthorizationDeniedException(
                    "This report does not exist or is not yours."));
            case MANAGER -> reports.findByIdAndProjectIdInAndDeletedFalse(id, assignedProjectIds(a))
                .orElseThrow(() -> new AuthorizationDeniedException(
                    "This report is outside the projects you manage."));
            case ADMIN -> reports.findByIdAndSubmissionStatusAndDeletedFalse(id, SubmissionStatus.APPROVED)
                .orElseThrow(() -> new AuthorizationDeniedException(
                    "Only approved reports are visible to you."));
        };
        if (a.role() == Role.INSPECTOR && !ec28Allows(report)) {
            throw new AuthorizationDeniedException(
                "This report is no longer visible: its project has been closed.");
        }
        return report;
    }

    private boolean ec28Allows(SafetyReport report) {
        ProjectSummary project = projectAccess
            .projectsByIds(List.of(report.getProjectId())).stream().findFirst().orElse(null);
        return project == null || project.open()
            || report.getProgressStatus() == ProgressStatus.CLOSED;
    }

    /**
     * FR-43 / AC-37: only the OWNING Inspector may open the edit form, and only
     * while the report is Rejected (no other state is editable).
     */
    @Transactional(readOnly = true)
    public SafetyReport editableReport(Long id) {
        if (role() != Role.INSPECTOR) {
            throw new AuthorizationDeniedException("Only Safety Team Inspectors can edit reports.");
        }
        SafetyReport report = detail(id); // ownership (BR-06) + EC-28
        if (report.getSubmissionStatus() != SubmissionStatus.REJECTED) {
            throw new BusinessRuleException("Only a rejected report can be edited and resubmitted.");
        }
        return report;
    }

    /** Photos attached to the given report's violations (authorised like detail). */
    @Transactional(readOnly = true)
    public List<PhotographEvidence> photosOfReport(Long reportId) {
        SafetyReport report = detail(reportId);
        return photos.forViolations(violationIdsOf(report));
    }

    /** One photograph, owned by one of the report's violations. */
    @Transactional(readOnly = true)
    public Optional<PhotographEvidence> photoOwnedBy(Long photoId, Long reportId) {
        SafetyReport report = detail(reportId);
        return photos.findOwnedBy(photoId, violationIdsOf(report));
    }

    /** Photograph bytes for the detail page — authorised as part of the fetch. */
    @Transactional(readOnly = true)
    public PhotoResource photo(Long reportId, Long photoId) {
        PhotographEvidence evidence = photoOwnedBy(photoId, reportId)
            .orElseThrow(() -> new AuthorizationDeniedException(
                "This photograph is not available for this report."));
        return new PhotoResource(evidence.getContentType(), photos.bytesOf(evidence));
    }

    public record PhotoResource(String contentType, byte[] content) {
    }

    /**
     * FR-44 version timeline. Visible to the project's Manager and the Admin
     * only — the Inspector sees the current version, never the history.
     */
    @Transactional(readOnly = true)
    public List<VersionItem> versionHistory(Long reportId) {
        SafetyReport report = detail(reportId); // authorisation first
        if (role() == Role.INSPECTOR) {
            return List.of();
        }
        return reportService.versionsOf(reportId).stream()
            .map(version -> new VersionItem(
                version.getVersionNumber(),
                version.getSubmittedAt(),
                version.getObservationDate(),
                version.getObservationTime(),
                version.getLocation(),
                version.getObservationType(),
                reportService.decodeViolations(version)))
            .toList();
    }

    // -----------------------------------------------------------------
    // Rejection notifications (FR-42)
    // -----------------------------------------------------------------

    /** Rejection notifications of the OWNING Inspector; never non-empty for another role. */
    @Transactional(readOnly = true)
    public List<NotificationItem> myNotifications() {
        Actor a = actor();
        if (a.role() != Role.INSPECTOR) {
            return List.of(); // FR-42: notifications exist for Inspectors only
        }
        return notifications.forInspector(a.email()).stream()
            .map(item -> new NotificationItem(
                item.getId(),
                reference(item.getReportId()),
                item.getReason(),
                item.getCreatedAt()))
            .toList();
    }

    // -----------------------------------------------------------------
    // internal helpers
    // -----------------------------------------------------------------

    private List<SafetyReport> visibleToProjects(Collection<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return List.of();
        }
        return reports.findByProjectIdInAndDeletedFalseOrderBySubmittedAtDesc(projectIds);
    }

    private Collection<Long> assignedProjectIds(Actor actor) {
        return projectAccess.projectsAssignedTo(actor.email()).stream()
            .map(ProjectSummary::id)
            .toList();
    }

    private Set<Long> violationIdsOf(SafetyReport report) {
        return report.getViolations().stream()
            .map(Violation::getId)
            .collect(Collectors.toSet());
    }

    private Map<Long, String> projectNameById(List<SafetyReport> scoped) {
        if (scoped.isEmpty()) {
            return Map.of();
        }
        return projectAccess.projectsByIds(scoped.stream()
                .map(SafetyReport::getProjectId)
                .collect(Collectors.toSet()))
            .stream()
            .collect(Collectors.toMap(ProjectSummary::id, ProjectSummary::name, (first, second) -> first));
    }

    private Map<Long, ProjectSummary> projectById(List<SafetyReport> scoped) {
        if (scoped.isEmpty()) {
            return Map.of();
        }
        return projectAccess.projectsByIds(scoped.stream()
                .map(SafetyReport::getProjectId)
                .collect(Collectors.toSet()))
            .stream()
            .collect(Collectors.toMap(ProjectSummary::id, project -> project, (first, second) -> first));
    }
}
