package hse.reporting.application.report;

import java.time.LocalDateTime;

/**
 * One rejection notification for the owning Inspector (FR-42, AC-12, AC-41).
 *
 * <p>The reference is derived from the report id in the view model — the
 * {@code notification} table stores only the id, never a duplicated reference.
 */
public record NotificationItem(Long id,
                               String reportReference,
                               String reason,
                               LocalDateTime createdAt) {
}
