package hse.reporting.web.report;

import com.hse.reporting.useraccess.access.AuthorizationDeniedException;
import hse.reporting.application.project.ProjectSelectionQuery;
import hse.reporting.application.report.ObserverProfileQuery;
import hse.reporting.application.report.ReportDecisionUseCase;
import hse.reporting.application.report.ReportFilter;
import hse.reporting.application.report.ReportSubmissionCommand;
import hse.reporting.application.report.ReportSubmissionResult;
import hse.reporting.application.report.ReportSubmissionUseCase;
import hse.reporting.application.report.ReportViewingUseCase;
import hse.reporting.application.report.ReportViewingUseCase.Scope;
import hse.reporting.common.BusinessRuleException;
import hse.reporting.config.ReportOptions;
import hse.reporting.photo.PhotoUpload;
import hse.reporting.photo.PhotographEvidence;
import hse.reporting.report.IncidentType;
import hse.reporting.report.SafetyReport;
import hse.reporting.report.SubmissionStatus;
import hse.reporting.report.Violation;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report creation (Inspector), viewing with filters (FR-14/15/16), Manager
 * decisions (FR-10/FR-11) and edit/resubmit of a rejected report (FR-43).
 *
 * <p>Visibility and state rules are decided in the use cases; this class only
 * binds HTTP to them (ASR-01). URL-level role rules live in {@code SecurityConfig}.
 */
@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportSubmissionUseCase submission;
    private final ReportViewingUseCase viewing;
    private final ReportDecisionUseCase decision;
    private final ProjectSelectionQuery projects;
    private final ObserverProfileQuery observers;
    private final ReportFormValidator formValidator;
    private final ReportOptions options;

    public ReportController(ReportSubmissionUseCase submission,
                            ReportViewingUseCase viewing,
                            ReportDecisionUseCase decision,
                            ProjectSelectionQuery projects,
                            ObserverProfileQuery observers,
                            ReportFormValidator formValidator,
                            ReportOptions options) {
        this.submission = submission;
        this.viewing = viewing;
        this.decision = decision;
        this.projects = projects;
        this.observers = observers;
        this.formValidator = formValidator;
        this.options = options;
    }

    // ------------------------------------------------------------------
    // Creation — Inspector only (FR-01…FR-08; URL rules + use case both check)
    // ------------------------------------------------------------------

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

    // ------------------------------------------------------------------
    // Listing with filters (FR-14/15/16)
    // ------------------------------------------------------------------

    /**
     * The report table for the signed-in role. The five filters (status,
     * project, Open/Closed, severity of incident, incident type) are applied
     * server-side INSIDE the use case, after the role-scoped query — they can
     * only narrow, never widen, what the actor may see.
     */
    @GetMapping
    public String list(@RequestParam(required = false) String status,
                       @RequestParam(required = false) Long projectId,
                       @RequestParam(required = false) String incidentType,
                       @RequestParam(required = false) String severity,
                       Model model) {
        model.addAttribute("reports", viewing.list(new ReportFilter(status, projectId, incidentType, severity)));
        model.addAttribute("statusOptions", viewing.statusOptions());
        model.addAttribute("projectOptions", viewing.projectOptions());
        model.addAttribute("incidentTypes", IncidentType.values());
        model.addAttribute("riskRatings", options.getRiskRatings());
        model.addAttribute("otherOption", options.getOtherOption());

        model.addAttribute("selectedStatus", status == null || status.isBlank() ? "ALL" : status);
        model.addAttribute("selectedProjectId", projectId);
        model.addAttribute("selectedIncidentType", incidentType);
        model.addAttribute("selectedSeverity", severity);
        return "report/list";
    }

    // ------------------------------------------------------------------
    // Detail (FR-14/15/16, FR-44) and Manager decisions (FR-10/FR-11)
    // ------------------------------------------------------------------

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        SafetyReport report = viewing.detail(id);
        Scope scope = viewing.currentScope();

        model.addAttribute("report", report);
        model.addAttribute("reference", ReportViewingUseCase.reference(id));
        model.addAttribute("projectName", viewing.projectName(report.getProjectId()));
        model.addAttribute("scope", scope);
        model.addAttribute("photos", viewing.photosOfReport(id));
        model.addAttribute("versions", viewing.versionHistory(id));

        // Rendering decisions only — the POST handlers re-check everything (ASR-01).
        model.addAttribute("canDecide",
            scope == Scope.MANAGER && report.getSubmissionStatus() == SubmissionStatus.PENDING_APPROVAL);
        model.addAttribute("canEdit",
            scope == Scope.INSPECTOR && report.getSubmissionStatus() == SubmissionStatus.REJECTED);
        return "report/detail";
    }

    /** FR-10 / AC-11: Pending Approval → Open. */
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            decision.approve(id);
            redirectAttributes.addFlashAttribute("decisionSuccess",
                "Report approved — it is now Open and visible to the HSE Engineer.");
            return "redirect:/reports/" + id;
        } catch (AuthorizationDeniedException | BusinessRuleException failure) {
            return detailWithError(id, model, failure.getMessage());
        }
    }

    /** FR-11 / FR-42 / AC-12: Pending Approval → Rejected + reason + notification. */
    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam(name = "rejectReason", required = false) String rejectReason,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            decision.reject(id, rejectReason);
            redirectAttributes.addFlashAttribute("decisionSuccess",
                "Report rejected — your reason was saved and the Inspector has been notified.");
            return "redirect:/reports/" + id;
        } catch (AuthorizationDeniedException | BusinessRuleException failure) {
            return detailWithError(id, model, failure.getMessage());
        }
    }

    private String detailWithError(Long id, Model model, String message) {
        model.addAttribute("decisionError", message);
        return detail(id, model);
    }

    // ------------------------------------------------------------------
    // Edit & resubmit of a rejected report (FR-43, AC-37)
    // ------------------------------------------------------------------

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        SafetyReport report;
        try {
            report = viewing.editableReport(id);
        } catch (AuthorizationDeniedException denial) {
            throw denial; // → 403 page (AccessDeniedAdvice)
        } catch (BusinessRuleException rule) {
            // FR-43: only a rejected report is editable — send the reader back
            // to the detail page instead of dead-ending on the form.
            redirectAttributes.addFlashAttribute("decisionError", rule.getMessage());
            return "redirect:/reports/" + id;
        }
        return renderEditForm(convertToForm(report), model, report);
    }

    @PostMapping("/{id}/resubmit")
    public String resubmit(@PathVariable Long id,
                           @Valid @ModelAttribute("reportForm") ReportForm form,
                           BindingResult binding,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        SafetyReport report;
        try {
            report = viewing.editableReport(id);
        } catch (AuthorizationDeniedException denial) {
            throw denial; // → 403 page (AccessDeniedAdvice)
        } catch (BusinessRuleException rule) {
            redirectAttributes.addFlashAttribute("decisionError", rule.getMessage());
            return "redirect:/reports/" + id;
        }

        formValidator.validate(form, binding);
        if (binding.hasErrors()) {
            if (form.getViolations().isEmpty()) {
                form.getViolations().add(new ViolationForm());
            }
            return renderEditForm(form, model, report);
        }

        try {
            ReportSubmissionResult result = submission.resubmit(id, toCommand(form));
            redirectAttributes.addFlashAttribute("reportId", result.reportId());
            redirectAttributes.addFlashAttribute("duplicateSubmission", result.duplicateSubmission());
            return "redirect:/reports/submitted";
        } catch (BusinessRuleException | AccessDeniedException failure) {
            model.addAttribute("formError", failure.getMessage());
            binding.reject("resubmit", failure.getMessage());
            return renderEditForm(form, model, report);
        }
    }

    // ------------------------------------------------------------------
    // Photograph evidence (FR-44)
    // ------------------------------------------------------------------

    /** Streams the stored bytes; authorised as part of the fetch (report-scoped). */
    @GetMapping("/{id}/photos/{photoId}")
    public ResponseEntity<byte[]> streamPhoto(@PathVariable Long id, @PathVariable Long photoId) {
        ReportViewingUseCase.PhotoResource photo = viewing.photo(id, photoId);
        MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            if (photo.contentType() != null && !photo.contentType().isBlank()) {
                contentType = MediaType.parseMediaType(photo.contentType());
            }
        } catch (HttpMessageNotReadableException | IllegalArgumentException ignored) {
            // unknown/absent type → octet-stream
        }
        return ResponseEntity.ok().contentType(contentType).body(photo.content());
    }

    // ------------------------------------------------------------------
    // form plumbing
    // ------------------------------------------------------------------

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

    /** Edit mode: same form, plus reportId, rejection reason and stored photographs. */
    private String renderEditForm(ReportForm form, Model model, SafetyReport report) {
        model.addAttribute("reportId", report.getId());
        model.addAttribute("reference", ReportViewingUseCase.reference(report.getId()));
        model.addAttribute("rejectionReason", report.getRejectionReason());
        model.addAttribute("existingPhotos", existingPhotosByIndex(report));
        return renderForm(form, model);
    }

    /**
     * Stored photographs grouped by violation POSITION (the edit form keeps
     * violations positionally, so index i is the same violation on re-render).
     */
    private Map<Integer, List<PhotoItem>> existingPhotosByIndex(SafetyReport report) {
        Map<Long, Integer> indexOfViolation = new HashMap<>();
        List<Violation> violations = report.getViolations();
        for (int index = 0; index < violations.size(); index++) {
            indexOfViolation.put(violations.get(index).getId(), index);
        }
        Map<Integer, List<PhotoItem>> byIndex = new HashMap<>();
        for (PhotographEvidence photo : viewing.photosOfReport(report.getId())) {
            Integer index = indexOfViolation.get(photo.getViolationId());
            if (index != null) {
                byIndex.computeIfAbsent(index, key -> new ArrayList<>())
                    .add(new PhotoItem(photo.getId(), photo.getOriginalFileName()));
            }
        }
        return byIndex;
    }

    public record PhotoItem(Long id, String originalFileName) {
    }

    /** Populate the form from the stored report (rejection edit, FR-43). */
    private ReportForm convertToForm(SafetyReport report) {
        ReportForm form = new ReportForm();
        form.setSubmissionToken(submission.newSubmissionToken());
        form.setProjectId(report.getProjectId());
        form.setObservationDate(report.getObservationDate());
        form.setObservationTime(report.getObservationTime());
        form.setLocation(report.getLocation());
        form.setObservationType(report.getObservationType());

        for (Violation violation : report.getViolations()) {
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
            form.getViolations().add(violationForm);
        }
        return form;
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
