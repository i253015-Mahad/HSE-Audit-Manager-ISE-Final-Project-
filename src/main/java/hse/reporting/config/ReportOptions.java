package hse.reporting.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Team-supplied value lists for the standardized SOR form (FR-03 / OQ-01).
 * The lists are intentionally externalized: the code does not invent HSE
 * observation hazards or risk ratings. The OTHER option is always available
 * and permits free text.
 */
@ConfigurationProperties(prefix = "hse.form")
public class ReportOptions {

    private String otherOption = "OTHER";
    private List<String> observationHazards = List.of();
    private List<String> riskRatings = List.of();

    public String getOtherOption() {
        return otherOption;
    }

    public void setOtherOption(String otherOption) {
        this.otherOption = otherOption;
    }

    public List<String> getObservationHazards() {
        return observationHazards;
    }

    public void setObservationHazards(List<String> observationHazards) {
        this.observationHazards = observationHazards == null ? List.of() : List.copyOf(observationHazards);
    }

    public List<String> getRiskRatings() {
        return riskRatings;
    }

    public void setRiskRatings(List<String> riskRatings) {
        this.riskRatings = riskRatings == null ? List.of() : List.copyOf(riskRatings);
    }

    public boolean isOther(String value) {
        return otherOption.equals(value);
    }

    public boolean isKnownObservationHazard(String value) {
        return observationHazards.contains(value);
    }

    public boolean isKnownRiskRating(String value) {
        return riskRatings.contains(value);
    }
}
