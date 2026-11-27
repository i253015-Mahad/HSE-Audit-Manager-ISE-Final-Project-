package hse.reporting.application.report;

import hse.reporting.photo.PhotoUpload;
import hse.reporting.report.IncidentType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Use-case input. The acting user is resolved from the security context, not from this object. */
public record ReportSubmissionCommand(
    String submissionToken,
    Long projectId,
    LocalDate incidentDate,
    LocalTime incidentTime,
    String location,
    IncidentType incidentType,
    List<ViolationSubmission> violations
) {

    public record ViolationSubmission(
        String description,
        String hazardCategory,
        String hazardCategoryOther,
        String severityLevel,
        String severityLevelOther,
        String immediateAction,
        LocalDate incidentDate,
        String location,
        IncidentType incidentType,
        List<PhotoUpload> photos
    ) {
    }
}
