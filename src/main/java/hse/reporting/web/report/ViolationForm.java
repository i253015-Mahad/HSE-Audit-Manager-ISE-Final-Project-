package hse.reporting.web.report;

import hse.reporting.report.IncidentType;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ViolationForm {

    private String description;
    private String hazardCategory;
    private String hazardCategoryOther;
    private String severityLevel;
    private String severityLevelOther;
    private String immediateAction;
    private List<MultipartFile> photos = new ArrayList<>();

    /* Not rendered in the standard form. Populated only by a crafted request;
       SafetyReportService rejects it unless it matches the report's incident. */
    private LocalDate incidentDate;
    private String location;
    private IncidentType incidentType;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHazardCategory() {
        return hazardCategory;
    }

    public void setHazardCategory(String hazardCategory) {
        this.hazardCategory = hazardCategory;
    }

    public String getHazardCategoryOther() {
        return hazardCategoryOther;
    }

    public void setHazardCategoryOther(String hazardCategoryOther) {
        this.hazardCategoryOther = hazardCategoryOther;
    }

    public String getSeverityLevel() {
        return severityLevel;
    }

    public void setSeverityLevel(String severityLevel) {
        this.severityLevel = severityLevel;
    }

    public String getSeverityLevelOther() {
        return severityLevelOther;
    }

    public void setSeverityLevelOther(String severityLevelOther) {
        this.severityLevelOther = severityLevelOther;
    }

    public String getImmediateAction() {
        return immediateAction;
    }

    public void setImmediateAction(String immediateAction) {
        this.immediateAction = immediateAction;
    }

    public List<MultipartFile> getPhotos() {
        return photos;
    }

    public void setPhotos(List<MultipartFile> photos) {
        this.photos = photos == null ? new ArrayList<>() : photos;
    }

    public LocalDate getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(LocalDate incidentDate) {
        this.incidentDate = incidentDate;
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
}
