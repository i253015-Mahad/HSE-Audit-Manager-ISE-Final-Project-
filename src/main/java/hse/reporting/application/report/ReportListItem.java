package hse.reporting.application.report;

import java.time.LocalDate;

/**
 * One row of a report listing — the only report shape the presentation layer
 * ever sees (AGENTS.md §8: modules hand out view data, never entities with
 * decision power, and never their own visibility rules).
 *
 * <p>{@code reference} is formatted as {@code RPT-%04d} here and is never
 * stored (plan.md §7). {@code status} is one of the four display values the
 * shared card renders: {@code Pending Approval}, {@code Open}, {@code Closed},
 * {@code Rejected}. {@code rejectionReason} is only ever populated while the
 * report is Rejected (FR-11 keeps the value for history, the view hides it).
 */
public record ReportListItem(Long id,
                              String reference,
                              String project,
                              LocalDate incidentDate,
                              String location,
                              String status,
                              String statusClass,
                              String rejectionReason) {

    /** Badge CSS class: {@code status-pending-approval} / {@code status-open} /
     *  {@code status-closed} / {@code status-rejected} — derived here so the
     *  templates only ever interpolate a value. */
    public static String deriveStatusClass(String status) {
        return status == null ? "" : "status-" + status.toLowerCase().replace(' ', '-');
    }
}
