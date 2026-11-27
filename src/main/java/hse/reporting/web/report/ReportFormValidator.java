package hse.reporting.web.report;

import hse.reporting.config.ReportOptions;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;

/** Form-level feedback for the SOR lists, OTHER free text and required violator details. */
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
        List<ViolationForm> violations = form.getViolations();
        for (int i = 0; i < violations.size(); i++) {
            ViolationForm violation = violations.get(i);
            String path = "violations[" + i + "]";
            requireText(errors, path + ".briefDescription", violation.getBriefDescription(),
                "Enter a brief description.");
            requireText(errors, path + ".actionRecommended", violation.getActionRecommended(),
                "Enter the action recommended.");
            requireText(errors, path + ".violatorName", violation.getViolatorName(),
                "Enter the violator's name.");
            requireText(errors, path + ".violatorBadgeNo", violation.getViolatorBadgeNo(),
                "Enter the violator's badge number.");
            requireText(errors, path + ".violatorTitle", violation.getViolatorTitle(),
                "Enter the violator's title.");
            requireText(errors, path + ".taskIncharge", violation.getTaskIncharge(),
                "Enter the task incharge.");

            validateChoice(errors, path + ".observationHazard", violation.getObservationHazard(),
                violation.getObservationHazardOther(), options.getObservationHazards(), "observation hazard");
            validateChoice(errors, path + ".riskRating", violation.getRiskRating(),
                violation.getRiskRatingOther(), options.getRiskRatings(), "risk rating");
        }
    }

    private void requireText(Errors errors, String field, String value, String message) {
        if (value == null || value.isBlank()) {
            errors.rejectValue(field, field + ".required", message);
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
