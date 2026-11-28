package hse.reporting.application.report;

import hse.reporting.photo.PhotoUpload;
import hse.reporting.report.IncidentType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Use-case input. Observer identity and Manager are resolved server-side, never from the form. */
public record ReportSubmissionCommand(
    String submissionToken,
    Long projectId,
    LocalDate observationDate,
    LocalTime observationTime,
    String location,
    IncidentType observationType,
    List<ViolationSubmission> violations
) {

    public record ViolationSubmission(
        String briefDescription,
        String observationHazard,
        String observationHazardOther,
        String riskRating,
        String riskRatingOther,
        String actionRecommended,
        String violatorName,
        String violatorBadgeNo,
        String violatorTitle,
        String taskIncharge,
        List<PhotoUpload> photos
    ) {
    }
}
