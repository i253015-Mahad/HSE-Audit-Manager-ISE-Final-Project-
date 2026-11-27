package hse.reporting.report;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Report content and lifecycle are owned by Safety Report Management. */
@Entity
@Table(name = "safety_report")
public class SafetyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "inspector_email", nullable = false)
    private String inspectorEmail;

    /** Incident identity is date + location + type (OQ-02); time is not part of it. */
    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @Column(name = "incident_time", nullable = false)
    private LocalTime incidentTime;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_type", nullable = false)
    private IncidentType incidentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "submission_status", nullable = false)
    private SubmissionStatus submissionStatus = SubmissionStatus.PENDING_APPROVAL;

    /** Null while Pending Approval or Rejected: no Open/Closed status (FR-07). */
    @Enumerated(EnumType.STRING)
    @Column(name = "progress_status")
    private ProgressStatus progressStatus;

    @Column(nullable = false)
    private boolean deleted;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Violation> violations = new ArrayList<>();

    protected SafetyReport() {
    }

    public SafetyReport(Long projectId, String inspectorEmail, LocalDate incidentDate, LocalTime incidentTime,
                        String location, IncidentType incidentType, LocalDateTime submittedAt) {
        this.projectId = projectId;
        this.inspectorEmail = inspectorEmail;
        this.incidentDate = incidentDate;
        this.incidentTime = incidentTime;
        this.location = location;
        this.incidentType = incidentType;
        this.submittedAt = submittedAt;
        this.submissionStatus = SubmissionStatus.PENDING_APPROVAL;
        this.progressStatus = null;
        this.deleted = false;
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getInspectorEmail() {
        return inspectorEmail;
    }

    public LocalDate getIncidentDate() {
        return incidentDate;
    }

    public LocalTime getIncidentTime() {
        return incidentTime;
    }

    public String getLocation() {
        return location;
    }

    public IncidentType getIncidentType() {
        return incidentType;
    }

    public SubmissionStatus getSubmissionStatus() {
        return submissionStatus;
    }

    public ProgressStatus getProgressStatus() {
        return progressStatus;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public List<Violation> getViolations() {
        return List.copyOf(violations);
    }

    public void addViolation(Violation violation) {
        violations.add(violation);
        violation.assignTo(this);
    }
}
