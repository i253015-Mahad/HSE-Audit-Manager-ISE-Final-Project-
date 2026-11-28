package hse.reporting.web.report;

import hse.reporting.report.IncidentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** SOR form binding. Observation identity is report-level; violations repeat the visual block. */
public class ReportForm {

    private Long projectId;

    @NotNull(message = "Enter the observation date.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate observationDate;

    @NotNull(message = "Enter the time of observation.")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime observationTime;

    @NotBlank(message = "Enter the location/area.")
    private String location;

    @NotNull(message = "Select the type of observation.")
    private IncidentType observationType;

    @Valid
    @Size(min = 1, message = "A report must contain at least one violation.")
    private List<ViolationForm> violations = new ArrayList<>();

    private String submissionToken;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public LocalDate getObservationDate() {
        return observationDate;
    }

    public void setObservationDate(LocalDate observationDate) {
        this.observationDate = observationDate;
    }

    public LocalTime getObservationTime() {
        return observationTime;
    }

    public void setObservationTime(LocalTime observationTime) {
        this.observationTime = observationTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public IncidentType getObservationType() {
        return observationType;
    }

    public void setObservationType(IncidentType observationType) {
        this.observationType = observationType;
    }

    public List<ViolationForm> getViolations() {
        return violations;
    }

    public void setViolations(List<ViolationForm> violations) {
        this.violations = violations == null ? new ArrayList<>() : violations;
    }

    public String getSubmissionToken() {
        return submissionToken;
    }

    public void setSubmissionToken(String submissionToken) {
        this.submissionToken = submissionToken;
    }
}
