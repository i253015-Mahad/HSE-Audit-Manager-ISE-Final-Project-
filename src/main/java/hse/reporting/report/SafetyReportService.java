package hse.reporting.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hse.reporting.common.BusinessRuleException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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
            if (draft.briefDescription() == null || draft.briefDescription().isBlank()) {
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
            if (reports.countDuplicates(excludedReportId, observationDate, location, observationType,
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
            report.addViolation(new Violation(
                draft.briefDescription().trim(),
                draft.observationHazard(),
                draft.observationHazardOther(),
                draft.riskRating(),
                draft.riskRatingOther(),
                draft.actionRecommended(),
                draft.violatorName().trim(),
                draft.violatorBadgeNo().trim(),
                draft.violatorTitle().trim(),
                draft.taskIncharge().trim()));
        }
        return report;
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

    private record ViolationSnapshot(String briefDescription, String observationHazard,
                                     String observationHazardOther, String riskRating, String riskRatingOther,
                                     String actionRecommended, String violatorName, String violatorBadgeNo,
                                     String violatorTitle, String taskIncharge) {
    }
}
