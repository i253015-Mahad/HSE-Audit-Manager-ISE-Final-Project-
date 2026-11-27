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

    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @Column(name = "incident_time", nullable = false)
    private LocalTime incidentTime;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_type", nullable = false)
    private IncidentType incidentType;

    @Column(name = "violations_json", nullable = false, columnDefinition = "TEXT")
    private String violationsJson;

    protected ReportVersion() {
    }

    public ReportVersion(Long reportId, int versionNumber, LocalDateTime submittedAt, Long projectId,
                         String inspectorEmail, LocalDate incidentDate, LocalTime incidentTime,
                         String location, IncidentType incidentType, String violationsJson) {
        this.reportId = reportId;
        this.versionNumber = versionNumber;
        this.submittedAt = submittedAt;
        this.projectId = projectId;
        this.inspectorEmail = inspectorEmail;
        this.incidentDate = incidentDate;
        this.incidentTime = incidentTime;
        this.location = location;
        this.incidentType = incidentType;
        this.violationsJson = violationsJson;
    }

    public Long getId() {
        return id;
    }

    public int getVersionNumber() {
        return versionNumber;
    }
}
