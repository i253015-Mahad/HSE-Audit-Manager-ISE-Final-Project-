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

/** SOR content and lifecycle are owned by Safety Report Management. */
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

    @Column(name = "observer_name", nullable = false)
    private String observerName;

    @Column(name = "observer_badge_no", nullable = false)
    private String observerBadgeNo;

    @Column(name = "observer_title", nullable = false)
    private String observerTitle;

    @Column(name = "observer_company", nullable = false)
    private String observerCompany;

    /** Snapshot of the selected project's Manager at submission (FR-03). */
    @Column(name = "manager_email", nullable = false)
    private String managerEmail;

    /** Observation identity is date + location + type (OQ-02); time is not part of it. */
    @Column(name = "observation_date", nullable = false)
    private LocalDate observationDate;

    @Column(name = "observation_time", nullable = false)
    private LocalTime observationTime;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "observation_type", nullable = false)
    private IncidentType observationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "submission_status", nullable = false)
    private SubmissionStatus submissionStatus = SubmissionStatus.PENDING_APPROVAL;

    /** Null while Pending Approval or Rejected: no Open/Closed status (FR-07). */
    @Enumerated(EnumType.STRING)
    @Column(name = "progress_status")
    private ProgressStatus progressStatus;

    /**
     * FR-11: written by the project's Manager at rejection and stored with the report.
     * It is displayed only while {@code submissionStatus = REJECTED}; it is retained
     * after resubmission (FR-43) so the row keeps its history, but never rendered again.
     */
    @Column(name = "rejection_reason", length = 2000)
    private String rejectionReason;

    @Column(nullable = false)
    private boolean deleted;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Violation> violations = new ArrayList<>();

    protected SafetyReport() {
    }

    public SafetyReport(Long projectId, String inspectorEmail, ObserverDetails observer, String managerEmail,
                        LocalDate observationDate, LocalTime observationTime, String location,
                        IncidentType observationType, LocalDateTime submittedAt) {
        this.projectId = projectId;
        this.inspectorEmail = inspectorEmail;
        this.observerName = observer.name();
        this.observerBadgeNo = observer.badgeNumber();
        this.observerTitle = observer.title();
        this.observerCompany = observer.company();
        this.managerEmail = managerEmail;
        this.observationDate = observationDate;
        this.observationTime = observationTime;
        this.location = location;
        this.observationType = observationType;
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

    public String getObserverName() {
        return observerName;
    }

    public String getObserverBadgeNo() {
        return observerBadgeNo;
    }

    public String getObserverTitle() {
        return observerTitle;
    }

    public String getObserverCompany() {
        return observerCompany;
    }

    public String getManagerEmail() {
        return managerEmail;
    }

    public LocalDate getObservationDate() {
        return observationDate;
    }

    public LocalTime getObservationTime() {
        return observationTime;
    }

    public String getLocation() {
        return location;
    }

    public IncidentType getObservationType() {
        return observationType;
    }

    public SubmissionStatus getSubmissionStatus() {
        return submissionStatus;
    }

    public ProgressStatus getProgressStatus() {
        return progressStatus;
    }

    public String getRejectionReason() {
        return rejectionReason;
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

    // ---------------------------------------------------------------------
    // Lifecycle mutators. Package-private on purpose: SafetyReportService is
    // the only caller and owns the rules (FR-09…FR-11, FR-43, BR-04…BR-06).
    // ---------------------------------------------------------------------

    /** FR-10: Pending Approval -> Open. */
    void approveNow() {
        submissionStatus = SubmissionStatus.APPROVED;
        progressStatus = ProgressStatus.OPEN;
    }

    /** FR-11: Pending Approval -> Rejected, reason stored with the report. */
    void rejectNow(String reason) {
        submissionStatus = SubmissionStatus.REJECTED;
        progressStatus = null;
        rejectionReason = reason;
    }

    /**
     * FR-43: Rejected -> Pending Approval. The observation identity and the
     * violation list are replaced by the Inspector's edited form; the project
     * and Manager snapshot follow the re-selected project under FR-04.
     */
    void resubmitNow(Long projectId, String managerEmail, LocalDate observationDate, LocalTime observationTime,
                     String location, IncidentType observationType, List<ViolationDraft> drafts,
                     LocalDateTime submittedAt) {
        this.projectId = projectId;
        this.managerEmail = managerEmail;
        this.observationDate = observationDate;
        this.observationTime = observationTime;
        this.location = location;
        this.observationType = observationType;
        this.submittedAt = submittedAt;
        this.submissionStatus = SubmissionStatus.PENDING_APPROVAL;
        this.progressStatus = null;
        applyDrafts(drafts);
    }

    /**
     * Retains violations positionally so their ids — and therefore their
     * photograph evidence — survive an edit; surplus entries are dropped.
     */
    private void applyDrafts(List<ViolationDraft> drafts) {
        for (int index = 0; index < drafts.size(); index++) {
            ViolationDraft draft = drafts.get(index);
            if (index < violations.size()) {
                violations.get(index).apply(draft);
            } else {
                addViolation(Violation.from(draft));
            }
        }
        for (int index = violations.size() - 1; index >= drafts.size(); index--) {
            violations.remove(index);
        }
    }
}
