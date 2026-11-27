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

public class ReportForm {

    private Long projectId;

    @NotNull(message = "Enter the incident date.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate incidentDate;

    @NotNull(message = "Enter the incident time.")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime incidentTime;

    @NotBlank(message = "Enter the incident location.")
    private String location;

    @NotNull(message = "Select the incident type.")
    private IncidentType incidentType;

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

    public LocalDate getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(LocalDate incidentDate) {
        this.incidentDate = incidentDate;
    }

    public LocalTime getIncidentTime() {
        return incidentTime;
    }

    public void setIncidentTime(LocalTime incidentTime) {
        this.incidentTime = incidentTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public IncidentType getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(IncidentType incidentType) {
        this.incidentType = incidentType;
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
