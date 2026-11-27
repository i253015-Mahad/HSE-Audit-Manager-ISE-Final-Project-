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
    public void validate(LocalDate incidentDate, String location, IncidentType incidentType,
                         List<ViolationDraft> drafts, Long excludedReportId) {
        if (drafts == null || drafts.isEmpty()) {
            throw new BusinessRuleException("A report must contain at least one violation.");
        }

        for (ViolationDraft draft : drafts) {
            if (draft.description() == null || draft.description().isBlank()) {
                throw new BusinessRuleException("Every violation needs a description.");
            }
            if (draft.hazardCategory() == null || draft.hazardCategory().isBlank()
                || draft.severityLevel() == null || draft.severityLevel().isBlank()) {
                throw new BusinessRuleException("Every violation needs a hazard category and severity level.");
            }
            if (isDifferentIncident(incidentDate, location, incidentType, draft)) {
                throw new BusinessRuleException("A report cannot combine violations from different incidents.");
            }
        }

        for (ViolationDraft draft : drafts) {
            if (reports.countDuplicates(excludedReportId, incidentDate, location, incidentType,
                    draft.description().trim()) > 0) {
                throw new BusinessRuleException(
                    "A violation with this incident and identical description already exists in an active report.");
            }
        }
    }

    public SafetyReport createReport(Long projectId, String inspectorEmail, LocalDate incidentDate,
                                     LocalTime incidentTime, String location, IncidentType incidentType,
                                     List<ViolationDraft> drafts, LocalDateTime submittedAt) {
        SafetyReport report = new SafetyReport(projectId, inspectorEmail, incidentDate, incidentTime,
            location, incidentType, submittedAt);
        for (ViolationDraft draft : drafts) {
            report.addViolation(new Violation(
                draft.description().trim(),
                draft.hazardCategory(),
                draft.hazardCategoryOther(),
                draft.severityLevel(),
                draft.severityLevelOther(),
                draft.immediateAction()));
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
            report.getIncidentDate(),
            report.getIncidentTime(),
            report.getLocation(),
            report.getIncidentType(),
            serializeViolations(report));
        versions.save(snapshot);
    }

    private boolean isDifferentIncident(LocalDate incidentDate, String location, IncidentType incidentType,
                                        ViolationDraft draft) {
        return (draft.incidentDate() != null && !draft.incidentDate().equals(incidentDate))
            || (draft.location() != null && !draft.location().equals(location))
            || (draft.incidentType() != null && draft.incidentType() != incidentType);
    }

    private String serializeViolations(SafetyReport report) {
        List<ViolationSnapshot> snapshots = report.getViolations().stream()
            .map(violation -> new ViolationSnapshot(
                violation.getDescription(),
                violation.getHazardCategory(),
                violation.getHazardCategoryOther(),
                violation.getSeverityLevel(),
                violation.getSeverityLevelOther(),
                violation.getImmediateAction()))
            .toList();
        try {
            return objectMapper.writeValueAsString(snapshots);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not record the report version snapshot", exception);
        }
    }

    private record ViolationSnapshot(String description, String hazardCategory, String hazardCategoryOther,
                                     String severityLevel, String severityLevelOther, String immediateAction) {
    }
}
