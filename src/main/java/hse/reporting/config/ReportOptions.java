package hse.reporting.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Team-supplied value lists for the standardized HSE form (FR-03 / OQ-01).
 * The lists are intentionally externalized: the code does not invent HSE
 * categories or severity levels. The OTHER option is always available and
 * permits free text.
 */
@ConfigurationProperties(prefix = "hse.form")
public class ReportOptions {

    private String otherOption = "OTHER";
    private List<String> hazardCategories = List.of();
    private List<String> severityLevels = List.of();

    public String getOtherOption() {
        return otherOption;
    }

    public void setOtherOption(String otherOption) {
        this.otherOption = otherOption;
    }

    public List<String> getHazardCategories() {
        return hazardCategories;
    }

    public void setHazardCategories(List<String> hazardCategories) {
        this.hazardCategories = hazardCategories == null ? List.of() : List.copyOf(hazardCategories);
    }

    public List<String> getSeverityLevels() {
        return severityLevels;
    }

    public void setSeverityLevels(List<String> severityLevels) {
        this.severityLevels = severityLevels == null ? List.of() : List.copyOf(severityLevels);
    }

    public boolean isOther(String value) {
        return otherOption.equals(value);
    }

    public boolean isKnownHazardCategory(String value) {
        return hazardCategories.contains(value);
    }

    public boolean isKnownSeverityLevel(String value) {
        return severityLevels.contains(value);
    }
}
