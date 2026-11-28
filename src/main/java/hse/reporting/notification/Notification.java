package hse.reporting.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * In-system rejection notification (FR-42, FR-11, AC-12).
 *
 * <p>Created only when a project's Manager rejects a pending report, and read
 * only by that report's Inspector — no other user receives it. The displayed
 * reference ({@code RPT-%04d}) is derived from {@link #reportId} in the view
 * model rather than stored, so it can never drift from the report itself.
 */
@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id", nullable = false)
    private Long reportId;

    @Column(name = "inspector_email", nullable = false)
    private String inspectorEmail;

    @Column(nullable = false, length = 2000)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Notification() {
    }

    public Notification(Long reportId, String inspectorEmail, String reason, LocalDateTime createdAt) {
        this.reportId = reportId;
        this.inspectorEmail = inspectorEmail;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getReportId() {
        return reportId;
    }

    public String getInspectorEmail() {
        return inspectorEmail;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
