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

@Entity
@Table(name = "safety_violation")
public class Violation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private SafetyReport report;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "hazard_category", nullable = false)
    private String hazardCategory;

    @Column(name = "hazard_category_other")
    private String hazardCategoryOther;

    @Column(name = "severity_level", nullable = false)
    private String severityLevel;

    @Column(name = "severity_level_other")
    private String severityLevelOther;

    @Column(name = "immediate_action", length = 2000)
    private String immediateAction;

    protected Violation() {
    }

    public Violation(String description, String hazardCategory, String hazardCategoryOther,
                     String severityLevel, String severityLevelOther, String immediateAction) {
        this.description = description;
        this.hazardCategory = hazardCategory;
        this.hazardCategoryOther = hazardCategoryOther;
        this.severityLevel = severityLevel;
        this.severityLevelOther = severityLevelOther;
        this.immediateAction = immediateAction;
    }

    public Long getId() {
        return id;
    }

    public SafetyReport getReport() {
        return report;
    }

    public String getDescription() {
        return description;
    }

    public String getHazardCategory() {
        return hazardCategory;
    }

    public String getHazardCategoryOther() {
        return hazardCategoryOther;
    }

    public String getSeverityLevel() {
        return severityLevel;
    }

    public String getSeverityLevelOther() {
        return severityLevelOther;
    }

    public String getImmediateAction() {
        return immediateAction;
    }

    public void assignTo(SafetyReport report) {
        this.report = report;
    }
}
