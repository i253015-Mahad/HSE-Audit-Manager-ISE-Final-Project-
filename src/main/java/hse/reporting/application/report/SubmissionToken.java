package hse.reporting.application.report;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** One-time form token: EC-12 / AC-10, exactly one report per submission. */
@Entity
@Table(name = "submission_token")
public class SubmissionToken {

    @Id
    private String token;

    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected SubmissionToken() {
    }

    public SubmissionToken(String token, LocalDateTime createdAt) {
        this.token = token;
        this.createdAt = createdAt;
    }

    public String getToken() {
        return token;
    }

    public Long getReportId() {
        return reportId;
    }

    public void markUsed(Long reportId) {
        this.reportId = reportId;
    }
}
