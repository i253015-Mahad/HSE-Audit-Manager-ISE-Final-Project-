package hse.reporting.application.report;

/**
 * User-selected filters of the report listing (FR-14/15/16).
 *
 * <p>Applied server-side AFTER the role-scoped query — a filter can only ever
 * narrow the actor's own visibility, never widen it (AGENTS.md §8).
 *
 * <p>{@code status} is one of the submitted dropdown values: {@code ALL} (or
 * empty), {@code PENDING_APPROVAL}, {@code REJECTED}, {@code OPEN},
 * {@code CLOSED}. {@code OPEN}/{@code CLOSED} mean APPROVED reports whose
 * progress is Open/Closed. {@code severity} matches a report when ANY of its
 * violations carries that risk rating; {@code incidentType} is
 * {@code ACCIDENT} or {@code INSPECTION_FINDING}.
 */
public record ReportFilter(String status,
                           Long projectId,
                           String incidentType,
                           String severity) {

    public static final ReportFilter NONE = new ReportFilter(null, null, null, null);

    public boolean hasStatus() {
        return status != null && !status.isBlank() && !"ALL".equals(status);
    }
}
