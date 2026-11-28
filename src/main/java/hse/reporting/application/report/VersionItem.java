package hse.reporting.application.report;

import hse.reporting.report.IncidentType;
import hse.reporting.report.SafetyReportService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * One immutable version snapshot as rendered on the detail page (FR-44).
 * Visible to the project's Manager and the Admin only.
 */
public record VersionItem(int versionNumber,
                          LocalDateTime submittedAt,
                          LocalDate observationDate,
                          LocalTime observationTime,
                          String location,
                          IncidentType observationType,
                          List<SafetyReportService.ViolationSnapshot> violations) {
}
