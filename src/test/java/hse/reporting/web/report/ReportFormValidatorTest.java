package hse.reporting.web.report;

import hse.reporting.config.ReportOptions;
import hse.reporting.report.IncidentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportFormValidatorTest {

    private ReportFormValidator validator;

    @BeforeEach
    void setUp() {
        ReportOptions options = new ReportOptions();
        options.setObservationHazards(List.of("Unsafe act", "Unsafe condition"));
        options.setRiskRatings(List.of("Low", "Medium", "High"));
        validator = new ReportFormValidator(options);
    }

    @Test
    void acceptsACompleteSorForm() {
        ReportForm form = validForm();
        BindingResult errors = errors(form);

        validator.validate(form, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    void otherChoicesRequireFreeText() {
        ReportForm form = validForm();
        form.getViolations().get(0).setObservationHazard("OTHER");
        form.getViolations().get(0).setRiskRating("OTHER");
        BindingResult errors = errors(form);

        validator.validate(form, errors);

        assertTrue(hasError(errors,"violations[0].observationHazardOther"));
        assertTrue(hasError(errors,"violations[0].riskRatingOther"));
    }

    @Test
    void everyViolatorFieldIsRequired() {
        ReportForm form = validForm();
        form.getViolations().get(0).setTaskIncharge("");
        form.getViolations().get(0).setViolatorBadgeNo(null);
        BindingResult errors = errors(form);

        validator.validate(form, errors);

        assertTrue(hasError(errors,"violations[0].taskIncharge"));
        assertTrue(hasError(errors,"violations[0].violatorBadgeNo"));
    }

    @Test
    void rejectsValuesOutsideTheConfiguredLists() {
        ReportForm form = validForm();
        form.getViolations().get(0).setRiskRating("Catastrophic");
        BindingResult errors = errors(form);

        validator.validate(form, errors);

        assertTrue(hasError(errors,"violations[0].riskRating"));
    }

    private BindingResult errors(ReportForm form) {
        return new BeanPropertyBindingResult(form, "reportForm");
    }

    private boolean hasError(BindingResult errors, String field) {
        return errors.getFieldErrors().stream().anyMatch(error -> field.equals(error.getField()));
    }

    private ReportForm validForm() {
        ReportForm form = new ReportForm();
        form.setProjectId(1L);
        form.setObservationDate(LocalDate.of(2026, 11, 27));
        form.setObservationTime(LocalTime.of(9, 30));
        form.setLocation("Site A");
        form.setObservationType(IncidentType.INSPECTION_FINDING);

        ViolationForm violation = new ViolationForm();
        violation.setBriefDescription("Unprotected edge");
        violation.setObservationHazard("Unsafe condition");
        violation.setRiskRating("High");
        violation.setActionRecommended("Install guardrails");
        violation.setViolatorName("Ali");
        violation.setViolatorBadgeNo("B-123");
        violation.setViolatorTitle("Labourer");
        violation.setTaskIncharge("Foreman");
        form.getViolations().add(violation);
        return form;
    }
}
