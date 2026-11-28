package hse.reporting.report;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** One SOR observation entry with its own violator details and photograph evidence. */
@Entity
@Table(name = "safety_violation")
public class Violation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private SafetyReport report;

    @Column(name = "brief_description", nullable = false, length = 2000)
    private String briefDescription;

    @Column(name = "observation_hazard", nullable = false)
    private String observationHazard;

    @Column(name = "observation_hazard_other")
    private String observationHazardOther;

    @Column(name = "risk_rating", nullable = false)
    private String riskRating;

    @Column(name = "risk_rating_other")
    private String riskRatingOther;

    @Column(name = "action_recommended", length = 2000)
    private String actionRecommended;

    @Column(name = "violator_name", nullable = false)
    private String violatorName;

    @Column(name = "violator_badge_no", nullable = false)
    private String violatorBadgeNo;

    @Column(name = "violator_title", nullable = false)
    private String violatorTitle;

    @Column(name = "task_incharge", nullable = false)
    private String taskIncharge;

    protected Violation() {
    }

    public Violation(String briefDescription, String observationHazard, String observationHazardOther,
                     String riskRating, String riskRatingOther, String actionRecommended,
                     String violatorName, String violatorBadgeNo, String violatorTitle, String taskIncharge) {
        this.briefDescription = briefDescription;
        this.observationHazard = observationHazard;
        this.observationHazardOther = observationHazardOther;
        this.riskRating = riskRating;
        this.riskRatingOther = riskRatingOther;
        this.actionRecommended = actionRecommended;
        this.violatorName = violatorName;
        this.violatorBadgeNo = violatorBadgeNo;
        this.violatorTitle = violatorTitle;
        this.taskIncharge = taskIncharge;
    }

    public Long getId() {
        return id;
    }

    public SafetyReport getReport() {
        return report;
    }

    public String getBriefDescription() {
        return briefDescription;
    }

    public String getObservationHazard() {
        return observationHazard;
    }

    public String getObservationHazardOther() {
        return observationHazardOther;
    }

    public String getRiskRating() {
        return riskRating;
    }

    public String getRiskRatingOther() {
        return riskRatingOther;
    }

    public String getActionRecommended() {
        return actionRecommended;
    }

    public String getViolatorName() {
        return violatorName;
    }

    public String getViolatorBadgeNo() {
        return violatorBadgeNo;
    }

    public String getViolatorTitle() {
        return violatorTitle;
    }

    public String getTaskIncharge() {
        return taskIncharge;
    }

    public void assignTo(SafetyReport report) {
        this.report = report;
    }

    /**
     * FR-03 block built from a validated draft, trimmed exactly the way a first
     * submission trims it so both paths produce identical rows.
     */
    static Violation from(ViolationDraft draft) {
        return new Violation(
            draft.briefDescription().trim(),
            draft.observationHazard(),
            draft.observationHazardOther(),
            draft.riskRating(),
            draft.riskRatingOther(),
            draft.actionRecommended(),
            draft.violatorName().trim(),
            draft.violatorBadgeNo().trim(),
            draft.violatorTitle().trim(),
            draft.taskIncharge().trim());
    }

    /** FR-43: edits in place so the id — and the photographs hanging off it — survive. */
    void apply(ViolationDraft draft) {
        this.briefDescription = draft.briefDescription().trim();
        this.observationHazard = draft.observationHazard();
        this.observationHazardOther = draft.observationHazardOther();
        this.riskRating = draft.riskRating();
        this.riskRatingOther = draft.riskRatingOther();
        this.actionRecommended = draft.actionRecommended();
        this.violatorName = draft.violatorName().trim();
        this.violatorBadgeNo = draft.violatorBadgeNo().trim();
        this.violatorTitle = draft.violatorTitle().trim();
        this.taskIncharge = draft.taskIncharge().trim();
    }
}
