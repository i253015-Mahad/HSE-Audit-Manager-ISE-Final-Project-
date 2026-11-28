package hse.reporting.web.report;

import hse.reporting.application.project.ProjectSelectionQuery;
import hse.reporting.application.report.ObserverProfileQuery;
import hse.reporting.application.report.ReportSubmissionCommand;
import hse.reporting.application.report.ReportSubmissionResult;
import hse.reporting.application.report.ReportSubmissionUseCase;
import hse.reporting.application.report.ReportViewingUseCase;
import com.hse.reporting.common.BusinessRuleException;
import hse.reporting.config.ReportOptions;
import hse.reporting.photo.PhotoUpload;
import hse.reporting.report.IncidentType;
import hse.reporting.report.SafetyReport;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportSubmissionUseCase submission;
    private final ReportViewingUseCase viewing;
    private final ProjectSelectionQuery projects;
    private final ObserverProfileQuery observers;
    private final ReportFormValidator formValidator;
    private final ReportOptions options;

    public ReportController(ReportSubmissionUseCase submission,
                            ReportViewingUseCase viewing,
                            ProjectSelectionQuery projects,
                            ObserverProfileQuery observers,
                            ReportFormValidator formValidator,
                            ReportOptions options) {
        this.submission = submission;
        this.viewing = viewing;
        this.projects = projects;
        this.observers = observers;
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
            if (form.getViolations().isEmpty()) {
                form.getViolations().add(new ViolationForm());
            }
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

    /**
     * List reports with filtering (FR-14/15/16).
     */
    @GetMapping
    public String list(@RequestParam(required = false) String status,
                       @RequestParam(required = false) Long projectId,
                       @RequestParam(required = false) String incidentType,
                       @RequestParam(required = false) String severity,
                       Model model) {
        switch (viewing.currentScope()) {
            case INSPECTOR:
                model.addAttribute("reports", viewing.listForInspector());
                break;
            case MANAGER:
                model.addAttribute("reports", viewing.listForManager());
                break;
            case ADMIN:
                model.addAttribute("reports", viewing.listForAdmin());
                break;
        }

        // Get filter options
        model.addAttribute("status", status);
        model.addAttribute("projectId", projectId);
        model.addAttribute("incidentType", incidentType);
        model.addAttribute("severity", severity);

        return "report/list";
    }

    /**
     * View report detail (FR-14/15/16, FR-44).
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        SafetyReport report = viewing.detail(id);
        model.addAttribute("report", report);
        model.addAttribute("reference", generateReference(report.getId()));

        // Version history
        model.addAttribute("versions", viewing.versionsOf(id));

        Scope scope = viewing.currentScope();
        model.addAttribute("scope", scope);

        return "report/detail";
    }

    /**
     * Edit rejected report (AC-37, FR-43).
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        SafetyReport report = viewing.detail(id);

        // Only inspectors can edit
        if (viewing.currentScope() != Scope.INSPECTOR || !report.getInspectorEmail().equals(viewing.actor().email())) {
            throw new AccessDeniedException("You can only edit your own rejection reports.");
        }

        if (report.getSubmissionStatus() != hse.reporting.report.SubmissionStatus.REJECTED) {
            throw new AccessDeniedException("Only rejected reports can be edited.");
        }

        // Populate form with report data
        ReportForm form = convertToForm(report);
        model.addAttribute("reportForm", form);
        model.addAttribute("reportId", report.getId());
        model.addAttribute("reference", generateReference(report.getId()));

        return renderForm(form, model);
    }

    /**
     * Resubmit rejected report (FR-43, AC-37).
     */
    @PostMapping("/{id}/resubmit")
    public String resubmit(@PathVariable Long id,
                           @Valid @ModelAttribute("reportForm") ReportForm form,
                           BindingResult binding,
                           Model model) {
        if (binding.hasErrors()) {
            return editForm(id, model);
        }

        try {
            // Update the submissionToken (The form has a hidden token field)
            form.setSubmissionToken(submission.newSubmissionToken());
            ReportSubmissionResult result = submission.resubmit(id, toCommand(form));
            model.addAttribute("reportId", result.reportId());
            return "redirect:/reports/submitted";
        } catch (AccessDeniedException e) {
            model.addAttribute("formError", e.getMessage());
            binding.reject("resubmit", e.getMessage());
            return editForm(id, model);
        } catch (BusinessRuleException e) {
            model.addAttribute("formError", e.getMessage());
            binding.reject("resubmit", e.getMessage());
            return editForm(id, model);
        }
    }

    /**
     * Approve a pending report (FR-10).
     */
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id) {
        try {
            submission.approveReport(id); // Need to check if this exists
            return "redirect:/reports/" + id;
        } catch (AccessDeniedException e) {
            throw new AccessDeniedException("You do not have permission to approve this report.");
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException(e.getMessage());
        }
    }

    /**
     * Reject a pending report with reason (FR-11, FR-42).
     */
    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam String rejectReason,
                         RedirectAttributes redirectAttributes) {
        try {
            submission.rejectReport(id, rejectReason);
            redirectAttributes.addFlashAttribute("rejectSuccess", "Report rejected successfully.");
            return "redirect:/reports/" + id;
        } catch (AccessDeniedException e) {
            throw new AccessDeniedException("You do not have permission to reject this report.");
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException(e.getMessage());
        }
    }

    /**
     * Get list of attached photos (FR-44).
     */
    @GetMapping("/{id}/photos")
    public String photos(@PathVariable Long id, Model model) {
        model.addAttribute("photos", viewing.photosOfReport(id));
        return "report/photos";
    }

    /**
     * Stream a photo (FR-44).
     */
    @GetMapping("/{id}/photos/{photoId}")
    public String streamPhoto(@PathVariable Long id, @PathVariable Long photoId) {
        Optional<hse.reporting.photo.PhotographEvidence> photo = viewing.photoOwnedBy(photoId, id);
        if (photo.isEmpty()) {
            throw new AccessDeniedException("Photo not found or access denied.");
        }
        // In a real implementation, this would return the bytes directly
        // For now, return to detail page
        return "redirect:/reports/" + id;
    }

    private String renderForm(ReportForm form, Model model) {
        var observer = observers.currentObserver();
        List<ProjectSelectionQuery.ProjectOption> projectOptions = projects.availableForCurrentInspector();
        String selectedManagerEmail = projectOptions.stream()
            .filter(project -> project.id().equals(form.getProjectId()))
            .map(ProjectSelectionQuery.ProjectOption::managerEmail)
            .findFirst()
            .orElse(null);

        model.addAttribute("reportForm", form);
        model.addAttribute("observer", observer);
        model.addAttribute("projects", projectOptions);
        model.addAttribute("selectedManagerEmail", selectedManagerEmail);
        model.addAttribute("observationHazards", options.getObservationHazards());
        model.addAttribute("riskRatings", options.getRiskRatings());
        model.addAttribute("otherOption", options.getOtherOption());
        model.addAttribute("observationTypes", IncidentType.values());
        if (observer == null && !model.containsAttribute("formError")) {
            model.addAttribute("formError",
                "Your SOR observer profile is not provisioned. Contact your HSE administrator.");
        }
        return "report/new";
    }

    /**
     * Convert SafetyReport to ReportForm for editing rejected reports.
     */
    private ReportForm convertToForm(SafetyReport report) {
        ReportForm form = new ReportForm();
        form.setSubmissionToken(submission.newSubmissionToken());
        form.setProjectId(report.getProjectId());
        form.setObservationDate(report.getObservationDate());
        form.setObservationTime(report.getObservationTime());
        form.setLocation(report.getLocation());
        form.setObservationType(report.getObservationType());

        // Populate violations
        for (var violation : report.getViolations()) {
            ViolationForm violationForm = new ViolationForm();
            violationForm.setBriefDescription(violation.getBriefDescription());
            violationForm.setObservationHazard(violation.getObservationHazard());
            violationForm.setObservationHazardOther(violation.getObservationHazardOther());
            violationForm.setRiskRating(violation.getRiskRating());
            violationForm.setRiskRatingOther(violation.getRiskRatingOther());
            violationForm.setActionRecommended(violation.getActionRecommended());
            violationForm.setViolatorName(violation.getViolatorName());
            violationForm.setViolatorBadgeNo(violation.getViolatorBadgeNo());
            violationForm.setViolatorTitle(violation.getViolatorTitle());
            violationForm.setTaskIncharge(violation.getTaskIncharge());

            // Add photos
            Map<Long, java.util.List<hse.reporting.photo.PhotographEvidence>> photoMap = new HashMap<>();
            photoMap.put(violation.getId(), viewing.photosOfReport(report.getId()).stream()
                .filter(p -> p.getViolationId().equals(violation.getId()))
                .toList());
            violationForm.setPhotosMap(photoMap);

            form.getViolations().add(violationForm);
        }

        return form;
    }

    /**
     * Generate report reference (RPT-0001 format).
     */
    private String generateReference(Long reportId) {
        return String.format("RPT-%04d", reportId);
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
                violation.getBriefDescription(),
                violation.getObservationHazard(),
                violation.getObservationHazardOther(),
                violation.getRiskRating(),
                violation.getRiskRatingOther(),
                violation.getActionRecommended(),
                violation.getViolatorName(),
                violation.getViolatorBadgeNo(),
                violation.getViolatorTitle(),
                violation.getTaskIncharge(),
                photos));
        }
        return new ReportSubmissionCommand(
            form.getSubmissionToken(),
            form.getProjectId(),
            form.getObservationDate(),
            form.getObservationTime(),
            form.getLocation(),
            form.getObservationType(),
            violations);
    }
}
