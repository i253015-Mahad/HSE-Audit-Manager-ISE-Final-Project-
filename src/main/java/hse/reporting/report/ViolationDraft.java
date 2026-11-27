package hse.reporting.report;

import java.time.LocalDate;

/**
 * Domain input for one safety violation (FR-03). Incident fields are optional:
 * the standard form has one incident header, so they are normally null. They
 * exist so the single-incident rule can still be enforced if another caller
 * supplies per-violation incident data.
 */
public record ViolationDraft(
    String description,
    String hazardCategory,
    String hazardCategoryOther,
    String severityLevel,
    String severityLevelOther,
    String immediateAction,
    LocalDate incidentDate,
    String location,
    IncidentType incidentType
) {
}
