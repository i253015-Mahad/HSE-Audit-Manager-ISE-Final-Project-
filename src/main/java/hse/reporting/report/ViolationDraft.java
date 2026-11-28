package hse.reporting.report;

/**
 * Domain input for one repeated SOR observation/violator block (FR-03).
 * Observation identity (date/time/location/type) is report-level, so it is
 * deliberately absent here.
 */
public record ViolationDraft(
    String briefDescription,
    String observationHazard,
    String observationHazardOther,
    String riskRating,
    String riskRatingOther,
    String actionRecommended,
    String violatorName,
    String violatorBadgeNo,
    String violatorTitle,
    String taskIncharge
) {
}
