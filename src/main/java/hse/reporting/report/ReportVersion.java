package hse.reporting.report;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Immutable snapshot recorded on every submission and resubmission (FR-44). */
@Entity
@Table(name = "report_version")
public class ReportVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id", nullable = false)
    private Long reportId;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

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

    @Column(name = "manager_email", nullable = false)
    private String managerEmail;

    @Column(name = "observation_date", nullable = false)
    private LocalDate observationDate;

    @Column(name = "observation_time", nullable = false)
    private LocalTime observationTime;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "observation_type", nullable = false)
    private IncidentType observationType;

    @Column(name = "violations_json", nullable = false, columnDefinition = "TEXT")
    private String violationsJson;

    protected ReportVersion() {
    }

    public ReportVersion(Long reportId, int versionNumber, LocalDateTime submittedAt, Long projectId,
                         String inspectorEmail, String observerName, String observerBadgeNo, String observerTitle,
                         String observerCompany, String managerEmail, LocalDate observationDate,
                         LocalTime observationTime, String location, IncidentType observationType,
                         String violationsJson) {
        this.reportId = reportId;
        this.versionNumber = versionNumber;
        this.submittedAt = submittedAt;
        this.projectId = projectId;
        this.inspectorEmail = inspectorEmail;
        this.observerName = observerName;
        this.observerBadgeNo = observerBadgeNo;
        this.observerTitle = observerTitle;
        this.observerCompany = observerCompany;
        this.managerEmail = managerEmail;
        this.observationDate = observationDate;
        this.observationTime = observationTime;
        this.location = location;
        this.observationType = observationType;
        this.violationsJson = violationsJson;
    }

    public Long getId() {
        return id;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public String getObserverName() {
        return observerName;
    }

    public String getManagerEmail() {
        return managerEmail;
    }

    public LocalDate getObservationDate() {
        return observationDate;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public LocalTime getObservationTime() {
        return observationTime;
    }

    public IncidentType getObservationType() {
        return observationType;
    }

    public String getViolationsJson() {
        return violationsJson;
    }
}
