package hse.reporting.web.report;

import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/** One repeated SOR observation/violator block and its photograph evidence. */
public class ViolationForm {

    private String briefDescription;
    private String observationHazard;
    private String observationHazardOther;
    private String riskRating;
    private String riskRatingOther;
    private String actionRecommended;
    private String violatorName;
    private String violatorBadgeNo;
    private String violatorTitle;
    private String taskIncharge;
    private List<MultipartFile> photos = new ArrayList<>();

    public String getBriefDescription() {
        return briefDescription;
    }

    public void setBriefDescription(String briefDescription) {
        this.briefDescription = briefDescription;
    }

    public String getObservationHazard() {
        return observationHazard;
    }

    public void setObservationHazard(String observationHazard) {
        this.observationHazard = observationHazard;
    }

    public String getObservationHazardOther() {
        return observationHazardOther;
    }

    public void setObservationHazardOther(String observationHazardOther) {
        this.observationHazardOther = observationHazardOther;
    }

    public String getRiskRating() {
        return riskRating;
    }

    public void setRiskRating(String riskRating) {
        this.riskRating = riskRating;
    }

    public String getRiskRatingOther() {
        return riskRatingOther;
    }

    public void setRiskRatingOther(String riskRatingOther) {
        this.riskRatingOther = riskRatingOther;
    }

    public String getActionRecommended() {
        return actionRecommended;
    }

    public void setActionRecommended(String actionRecommended) {
        this.actionRecommended = actionRecommended;
    }

    public String getViolatorName() {
        return violatorName;
    }

    public void setViolatorName(String violatorName) {
        this.violatorName = violatorName;
    }

    public String getViolatorBadgeNo() {
        return violatorBadgeNo;
    }

    public void setViolatorBadgeNo(String violatorBadgeNo) {
        this.violatorBadgeNo = violatorBadgeNo;
    }

    public String getViolatorTitle() {
        return violatorTitle;
    }

    public void setViolatorTitle(String violatorTitle) {
        this.violatorTitle = violatorTitle;
    }

    public String getTaskIncharge() {
        return taskIncharge;
    }

    public void setTaskIncharge(String taskIncharge) {
        this.taskIncharge = taskIncharge;
    }

    public List<MultipartFile> getPhotos() {
        return photos;
    }

    public void setPhotos(List<MultipartFile> photos) {
        this.photos = photos == null ? new ArrayList<>() : photos;
    }
}
