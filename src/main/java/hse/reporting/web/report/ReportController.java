package hse.reporting.web.report;

import hse.reporting.application.project.ProjectSelectionQuery;
import hse.reporting.application.report.ReportSubmissionCommand;
import hse.reporting.application.report.ReportSubmissionResult;
import hse.reporting.application.report.ReportSubmissionUseCase;
import hse.reporting.common.BusinessRuleException;
import hse.reporting.config.ReportOptions;
import hse.reporting.photo.PhotoUpload;
import hse.reporting.report.IncidentType;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportSubmissionUseCase submission;
    private final ProjectSelectionQuery projects;
    private final ReportFormValidator formValidator;
    private final ReportOptions options;

    public ReportController(ReportSubmissionUseCase submission,
                            ProjectSelectionQuery projects,
                            ReportFormValidator formValidator,
                            ReportOptions options) {
        this.submission = submission;
        this.projects = projects;
        this.formValidator = formValidator;
        this.options = options;
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        ReportForm form = new ReportForm();
        form.getViolations().add(new ViolationForm());
        form.setSubmissionToken(submission.newSubmissionToken());
        return renderForm(form, model);
    }

    @PostMapping
    public String submit(@Valid @ModelAttribute("reportForm") ReportForm form,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        formValidator.validate(form, binding);
        if (binding.hasErrors()) {
            return renderForm(form, model);
        }

        try {
            ReportSubmissionResult result = submission.submit(toCommand(form));
            redirectAttributes.addFlashAttribute("reportId", result.reportId());
            redirectAttributes.addFlashAttribute("duplicateSubmission", result.duplicateSubmission());
            return "redirect:/reports/submitted";
        } catch (BusinessRuleException | AccessDeniedException failure) {
            model.addAttribute("formError", failure.getMessage());
            binding.reject("submission", failure.getMessage());
            return renderForm(form, model);
        }
    }

    @GetMapping("/submitted")
    public String submitted() {
        return "report/submitted";
    }

    private String renderForm(ReportForm form, Model model) {
        model.addAttribute("reportForm", form);
        model.addAttribute("projects", projects.availableForCurrentInspector());
        model.addAttribute("hazardCategories", options.getHazardCategories());
        model.addAttribute("severityLevels", options.getSeverityLevels());
        model.addAttribute("otherOption", options.getOtherOption());
        model.addAttribute("incidentTypes", IncidentType.values());
        return "report/new";
    }

    private ReportSubmissionCommand toCommand(ReportForm form) {
        List<ReportSubmissionCommand.ViolationSubmission> violations = new ArrayList<>();
        for (ViolationForm violation : form.getViolations()) {
            List<PhotoUpload> photos = new ArrayList<>();
            for (MultipartFile file : violation.getPhotos()) {
                if (file != null && !file.isEmpty()) {
                    try {
                        photos.add(new PhotoUpload(file.getOriginalFilename(), file.getContentType(), file.getBytes()));
                    } catch (IOException exception) {
                        throw new BusinessRuleException("A photograph could not be read. Please select it again.");
                    }
                }
            }
            violations.add(new ReportSubmissionCommand.ViolationSubmission(
                violation.getDescription(),
                violation.getHazardCategory(),
                violation.getHazardCategoryOther(),
                violation.getSeverityLevel(),
                violation.getSeverityLevelOther(),
                violation.getImmediateAction(),
                violation.getIncidentDate(),
                violation.getLocation(),
                violation.getIncidentType(),
                photos));
        }
        return new ReportSubmissionCommand(
            form.getSubmissionToken(),
            form.getProjectId(),
            form.getIncidentDate(),
            form.getIncidentTime(),
            form.getLocation(),
            form.getIncidentType(),
            violations);
    }
}
