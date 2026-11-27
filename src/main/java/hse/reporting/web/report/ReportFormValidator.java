package hse.reporting.web.report;

import hse.reporting.config.ReportOptions;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;

/** Form-level feedback for the team-supplied lists and their OTHER free-text option. */
@Component
public class ReportFormValidator implements Validator {

    private final ReportOptions options;

    public ReportFormValidator(ReportOptions options) {
        this.options = options;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return ReportForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ReportForm form = (ReportForm) target;
        if (form.getProjectId() == null) {
            errors.rejectValue("projectId", "project.required", "Select a project.");
        }
        if (form.getIncidentType() == null) {
            errors.rejectValue("incidentType", "incidentType.required", "Select the incident type.");
        }

        List<ViolationForm> violations = form.getViolations();
        for (int i = 0; i < violations.size(); i++) {
            ViolationForm violation = violations.get(i);
            String path = "violations[" + i + "]";
            if (violation.getDescription() == null || violation.getDescription().isBlank()) {
                errors.rejectValue(path + ".description", "description.required", "Enter a violation description.");
            }
            validateChoice(errors, path + ".hazardCategory", violation.getHazardCategory(),
                violation.getHazardCategoryOther(), options.getHazardCategories(), "hazard category");
            validateChoice(errors, path + ".severityLevel", violation.getSeverityLevel(),
                violation.getSeverityLevelOther(), options.getSeverityLevels(), "severity level");
        }
    }

    private void validateChoice(Errors errors, String field, String selected, String otherText,
                                List<String> allowed, String label) {
        if (selected == null || selected.isBlank()) {
            errors.rejectValue(field, field + ".required", "Select a " + label + ".");
            return;
        }
        if (options.isOther(selected)) {
            if (otherText == null || otherText.isBlank()) {
                errors.rejectValue(field + "Other", field + ".otherRequired",
                    "Enter the " + label + " when choosing Other.");
            }
            return;
        }
        if (!allowed.contains(selected)) {
            errors.rejectValue(field, field + ".invalid", "Choose a listed " + label + " or Other.");
        }
    }
}
