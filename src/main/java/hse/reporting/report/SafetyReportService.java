package hse.reporting.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hse.reporting.common.BusinessRuleException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Domain rules and lifecycle state owned by Safety Report Management. */
@Service
public class SafetyReportService {

    private final SafetyReportRepository reports;
    private final ReportVersionRepository versions;
    private final ObjectMapper objectMapper;

    public SafetyReportService(SafetyReportRepository reports,
                               ReportVersionRepository versions,
                               ObjectMapper objectMapper) {
        this.reports = reports;
        this.versions = versions;
        this.objectMapper = objectMapper;
    }

    /**
     * FR-02, FR-05 and FR-06. {@code excludedReportId} is the report being
     * edited/resubmitted (Slice 4); it is null for a first submission.
     */
    public void validate(LocalDate observationDate, String location, IncidentType observationType,
                         List<ViolationDraft> drafts, Long excludedReportId) {
        if (drafts == null || drafts.isEmpty()) {
            throw new BusinessRuleException("A report must contain at least one violation.");
        }

        for (ViolationDraft draft : drafts) {
            String briefDescription = draft.briefDescription();
            if (briefDescription == null || briefDescription.trim().isBlank()) {
                throw new BusinessRuleException("Every violation needs a brief description.");
            }
            if (draft.observationHazard() == null || draft.observationHazard().isBlank()
                || draft.riskRating() == null || draft.riskRating().isBlank()) {
                throw new BusinessRuleException("Every violation needs an observation hazard and risk rating.");
            }
            if (draft.violatorName() == null || draft.violatorName().isBlank()
                || draft.violatorBadgeNo() == null || draft.violatorBadgeNo().isBlank()
                || draft.violatorTitle() == null || draft.violatorTitle().isBlank()
                || draft.taskIncharge() == null || draft.taskIncharge().isBlank()) {
                throw new BusinessRuleException("Every violation needs complete violator details.");
            }
        }

        for (ViolationDraft draft : drafts) {
            if (draft.briefDescription() != null && reports.countDuplicates(excludedReportId, observationDate, location, observationType,
                    draft.briefDescription().trim()) > 0) {
                throw new BusinessRuleException(
                    "A violation with this observation and identical description already exists in an active report.");
            }
        }
    }

    public SafetyReport createReport(Long projectId, String inspectorEmail, ObserverDetails observer,
                                     String managerEmail, LocalDate observationDate, LocalTime observationTime,
                                     String location, IncidentType observationType,
                                     List<ViolationDraft> drafts, LocalDateTime submittedAt) {
        SafetyReport report = new SafetyReport(projectId, inspectorEmail, observer, managerEmail,
            observationDate, observationTime, location, observationType, submittedAt);
        for (ViolationDraft draft : drafts) {
            report.addViolation(Violation.from(draft));
        }
        return report;
    }

    // ------------------------------------------------------------------
    // Lifecycle (FR-09…FR-11, FR-43, BR-04…BR-06)
    // ------------------------------------------------------------------

    /** FR-10: Pending Approval becomes Open, which is when Admin visibility starts (AC-11). */
    public void approve(SafetyReport report) {
        requireLive(report);
        if (report.getSubmissionStatus() != SubmissionStatus.PENDING_APPROVAL) {
            throw new BusinessRuleException("Only a report awaiting approval can be approved.");
        }
        report.approveNow();
    }

    /** FR-11 / FR-42: Pending Approval becomes Rejected with the reason stored on the report. */
    public void reject(SafetyReport report, String reason) {
        requireLive(report);
        if (report.getSubmissionStatus() != SubmissionStatus.PENDING_APPROVAL) {
            throw new BusinessRuleException("Only a report awaiting approval can be rejected.");
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleException("Enter a reason for rejecting this report.");
        }
        report.rejectNow(reason.trim());
    }

    /**
     * FR-43: the owning Inspector's edited form returns a Rejected report to
     * Pending Approval. The caller stores the new version snapshot (FR-44)
     * and the photographs for the retained and newly added violations.
     */
    public void resubmit(SafetyReport report, Resubmission resubmission) {
        requireLive(report);
        if (report.getSubmissionStatus() != SubmissionStatus.REJECTED) {
            throw new BusinessRuleException("Only a rejected report can be edited and resubmitted.");
        }
        report.resubmitNow(resubmission.projectId(), resubmission.managerEmail(),
            resubmission.observationDate(), resubmission.observationTime(), resubmission.location(),
            resubmission.observationType(), resubmission.violations(), resubmission.submittedAt());
    }

    private void requireLive(SafetyReport report) {
        if (report.isDeleted()) {
            throw new BusinessRuleException("This report is in the Deleted tab and cannot be changed.");
        }
    }

    // ------------------------------------------------------------------
    // Role-scoped reads (FR-14, FR-15, FR-16). Which scope applies to the
    // acting user is decided by the application layer, never by the view.
    // ------------------------------------------------------------------

    /** FR-14 / AC-14: an Inspector sees only their own non-deleted reports. */
    public List<SafetyReport> visibleToInspector(String inspectorEmail) {
        return reports.findByInspectorEmailAndDeletedFalseOrderBySubmittedAtDesc(inspectorEmail);
    }

    public Optional<SafetyReport> visibleToInspector(Long id, String inspectorEmail) {
        return reports.findByIdAndInspectorEmailAndDeletedFalse(id, inspectorEmail);
    }

    /** FR-15 / AC-15: a Manager sees the reports of the projects they handle. */
    public List<SafetyReport> visibleToProjects(Collection<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return List.of();
        }
        return reports.findByProjectIdInAndDeletedFalseOrderBySubmittedAtDesc(projectIds);
    }

    public Optional<SafetyReport> visibleToProjects(Long id, Collection<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Optional.empty();
        }
        return reports.findByIdAndProjectIdInAndDeletedFalse(id, projectIds);
    }

    /** FR-16 / AC-16: approved and non-deleted only — never Pending or Rejected (FR-12). */
    public List<SafetyReport> approvedNonDeleted() {
        return reports.findBySubmissionStatusAndDeletedFalseOrderBySubmittedAtDesc(SubmissionStatus.APPROVED);
    }

    public Optional<SafetyReport> approvedNonDeleted(Long id) {
        return reports.findByIdAndSubmissionStatusAndDeletedFalse(id, SubmissionStatus.APPROVED);
    }

    /**
     * Input for FR-43. {@code submittedAt} is the resubmission instant, which is
     * also what the new version snapshot records.
     */
    public record Resubmission(Long projectId, String managerEmail, LocalDate observationDate,
                               LocalTime observationTime, String location, IncidentType observationType,
                               List<ViolationDraft> violations, LocalDateTime submittedAt) {
    }

    /** FR-44: an immutable snapshot at each submission and resubmission. */
    public void recordVersion(SafetyReport report) {
        int nextVersion = versions.findByReportIdOrderByVersionNumberAsc(report.getId()).size() + 1;
        ReportVersion snapshot = new ReportVersion(
            report.getId(),
            nextVersion,
            report.getSubmittedAt(),
            report.getProjectId(),
            report.getInspectorEmail(),
            report.getObserverName(),
            report.getObserverBadgeNo(),
            report.getObserverTitle(),
            report.getObserverCompany(),
            report.getManagerEmail(),
            report.getObservationDate(),
            report.getObservationTime(),
            report.getLocation(),
            report.getObservationType(),
            serializeViolations(report));
        versions.save(snapshot);
    }

    /**
     * FR-44: version snapshots of one report, oldest first. Callers must have
     * already established that the actor may see this report.
     */
    public List<ReportVersion> versionsOf(Long reportId) {
        return versions.findByReportIdOrderByVersionNumberAsc(reportId);
    }

    /** Decodes a snapshot's violation JSON so FR-44 history can be rendered. */
    public List<ViolationSnapshot> decodeViolations(ReportVersion version) {
        try {
            ViolationSnapshot[] decoded = objectMapper.readValue(version.getViolationsJson(),
                ViolationSnapshot[].class);
            return List.of(decoded);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not read the report version snapshot", exception);
        }
    }

    private String serializeViolations(SafetyReport report) {
        List<ViolationSnapshot> snapshots = report.getViolations().stream()
            .map(violation -> new ViolationSnapshot(
                violation.getBriefDescription(),
                violation.getObservationHazard(),
                violation.getObservationHazardOther(),
                violation.getRiskRating(),
                violation.getRiskRatingOther(),
                violation.getActionRecommended(),
                violation.getViolatorName(),
                violation.getViolatorBadgeNo(),
                violation.getViolatorTitle(),
                violation.getTaskIncharge()))
            .toList();
        try {
            return objectMapper.writeValueAsString(snapshots);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not record the report version snapshot", exception);
        }
    }

    /** One violation as frozen into a version snapshot (FR-44). */
    public record ViolationSnapshot(String briefDescription, String observationHazard,
                                     String observationHazardOther, String riskRating, String riskRatingOther,
                                     String actionRecommended, String violatorName, String violatorBadgeNo,
                                     String violatorTitle, String taskIncharge) {
    }
}
