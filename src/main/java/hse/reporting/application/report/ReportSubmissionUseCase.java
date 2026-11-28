package hse.reporting.application.report;

import hse.reporting.application.ports.AuthorizationPort;
import hse.reporting.application.ports.ProjectEligibilityPort;
import hse.reporting.common.BusinessRuleException;
import hse.reporting.photo.PhotoEvidenceQuery;
import hse.reporting.photo.PhotoStore;
import hse.reporting.photo.PhotoUpload;
import hse.reporting.photo.PhotoValidator;
import hse.reporting.report.ObserverDetails;
import hse.reporting.report.SafetyReport;
import hse.reporting.report.SafetyReportRepository;
import hse.reporting.report.SafetyReportService;
import hse.reporting.report.SubmissionStatus;
import hse.reporting.report.Violation;
import hse.reporting.report.ViolationDraft;
import hse.reporting.user.CurrentActorService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Coordinates FR-01…FR-08 and FR-43 across the owning modules in one transaction. */
@Service
public class ReportSubmissionUseCase {

    private final AuthorizationPort authorization;
    private final ProjectEligibilityPort projectEligibility;
    private final CurrentActorService currentActor;
    private final ObserverProfileQuery observers;
    private final SafetyReportService reportService;
    private final SafetyReportRepository reports;
    private final PhotoValidator photoValidator;
    private final PhotoStore photoStore;
    private final PhotoEvidenceQuery photoEvidence;
    private final SubmissionTokenRepository tokens;

    public ReportSubmissionUseCase(AuthorizationPort authorization,
                                   ProjectEligibilityPort projectEligibility,
                                   CurrentActorService currentActor,
                                   ObserverProfileQuery observers,
                                   SafetyReportService reportService,
                                   SafetyReportRepository reports,
                                   PhotoValidator photoValidator,
                                   PhotoStore photoStore,
                                   PhotoEvidenceQuery photoEvidence,
                                   SubmissionTokenRepository tokens) {
        this.authorization = authorization;
        this.projectEligibility = projectEligibility;
        this.currentActor = currentActor;
        this.observers = observers;
        this.reportService = reportService;
        this.reports = reports;
        this.photoValidator = photoValidator;
        this.photoStore = photoStore;
        this.photoEvidence = photoEvidence;
        this.tokens = tokens;
    }

    @Transactional
    public String newSubmissionToken() {
        SubmissionToken token = new SubmissionToken(UUID.randomUUID().toString(), LocalDateTime.now());
        return tokens.save(token).getToken();
    }

    @Transactional
    public ReportSubmissionResult submit(ReportSubmissionCommand command) {
        String inspectorEmail = currentActor.email();
        if (!authorization.canSubmitReport(inspectorEmail)) {
            throw new AccessDeniedException("Only Safety Team Inspectors can create and submit reports.");
        }

        ProjectEligibilityPort.Eligibility eligibility =
            projectEligibility.check(inspectorEmail, command.projectId());
        if (!eligibility.allowed()) {
            throw new BusinessRuleException(eligibility.refusalMessage());
        }

        // Pessimistic lock: a second POST waits, then sees the claimed token and creates nothing.
        Optional<SubmissionToken> found = tokens.findForUpdate(command.submissionToken());
        if (found.isEmpty()) {
            throw new BusinessRuleException("This submission form is no longer valid. Reload the form and try again.");
        }
        SubmissionToken token = found.get();
        if (token.getReportId() != null) {
            return new ReportSubmissionResult(token.getReportId(), true);
        }

        ObserverDetails observer = observers.requireCurrentObserver();

        List<ReportSubmissionCommand.ViolationSubmission> submittedViolations = command.violations();
        if (submittedViolations == null) {
            submittedViolations = List.of();
        }
        List<ViolationDraft> drafts = toDrafts(submittedViolations);

        reportService.validate(command.observationDate(), command.location(), command.observationType(),
            drafts, null);
        for (ReportSubmissionCommand.ViolationSubmission violation : submittedViolations) {
            photoValidator.validate(violation.photos());
        }

        LocalDateTime submittedAt = LocalDateTime.now();
        SafetyReport report = reportService.createReport(command.projectId(), inspectorEmail, observer,
            eligibility.managerEmail(), command.observationDate(), command.observationTime(),
            command.location(), command.observationType(), drafts, submittedAt);
        SafetyReport saved = reports.saveAndFlush(report);

        // FR-44: the first immutable snapshot is taken at the moment of submission.
        reportService.recordVersion(saved);

        storePhotos(saved, submittedViolations);
        return new ReportSubmissionResult(saved.getId(), false);
    }

    /**
     * Resubmit a rejected report (FR-43, AC-37, FR-06, FR-22).
     *
     * <p>Checks:
     * <ul>
     *   <li>Actor is the owning Inspector (BR-06)</li>
     *   <li>Report is REJECTED — no other state is editable (FR-43)</li>
     *   <li>Project is OPEN and handled by the Inspector's Manager (FR-04/FR-22)</li>
     *   <li>Full revalidation FR-02…FR-06 with {@code excludedReportId = reportId} (FR-06 excludes self)</li>
     * </ul>
     *
     * <p>Side effects: the SAME report row returns to Pending Approval with its
     * observation identity and violations replaced in place (violation ids — and
     * therefore their photographs — survive positionally), and a new version
     * snapshot is recorded (FR-44). The stored rejection reason is retained but
     * is only ever displayed while the report is Rejected.
     */
    @Transactional
    public ReportSubmissionResult resubmit(Long reportId, ReportSubmissionCommand command) {
        String inspectorEmail = currentActor.email();
        if (!authorization.canSubmitReport(inspectorEmail)) {
            throw new AccessDeniedException("Only Safety Team Inspectors can edit and resubmit reports.");
        }

        SafetyReport report = reports.findByIdAndDeletedFalse(reportId)
            .orElseThrow(() -> new BusinessRuleException("This report no longer exists."));

        if (!report.getInspectorEmail().equals(inspectorEmail)) {
            throw new AccessDeniedException("You can only edit and resubmit your own reports.");
        }

        if (report.getSubmissionStatus() != SubmissionStatus.REJECTED) {
            throw new BusinessRuleException(
                "Only a rejected report can be edited and resubmitted; this report is "
                    + report.getSubmissionStatus().name().toLowerCase().replace('_', ' ') + ".");
        }

        ProjectEligibilityPort.Eligibility eligibility =
            projectEligibility.check(inspectorEmail, command.projectId());
        if (!eligibility.allowed()) {
            throw new BusinessRuleException(eligibility.refusalMessage());
        }

        ObserverDetails observer = observers.requireCurrentObserver();
        List<ReportSubmissionCommand.ViolationSubmission> submittedViolations = command.violations();
        if (submittedViolations == null) {
            submittedViolations = List.of();
        }
        List<ViolationDraft> drafts = toDrafts(submittedViolations);

        // FR-06: identical duplicate check excludes this very report.
        reportService.validate(command.observationDate(), command.location(), command.observationType(),
            drafts, reportId);

        // Validate new photographs and hold the per-violation cap including the
        // evidence the edit keeps (violations are retained positionally, so index i
        // is the violation that already sits at index i).
        List<Violation> retained = report.getViolations();
        for (int index = 0; index < submittedViolations.size(); index++) {
            List<PhotoUpload> uploads = submittedViolations.get(index).photos();
            photoValidator.validate(uploads);
            if (uploads != null && !uploads.isEmpty() && index < retained.size()) {
                long alreadyStored = photoEvidence.countFor(retained.get(index).getId());
                if (alreadyStored + uploads.size() > PhotoValidator.MAX_PHOTOS_PER_VIOLATION) {
                    throw new BusinessRuleException("A violation can hold at most "
                        + PhotoValidator.MAX_PHOTOS_PER_VIOLATION + " photographs.");
                }
            }
        }

        LocalDateTime submittedAt = LocalDateTime.now();
        reportService.resubmit(report, new SafetyReportService.Resubmission(
            command.projectId(), eligibility.managerEmail(), command.observationDate(),
            command.observationTime(), command.location(), command.observationType(),
            drafts, submittedAt));
        SafetyReport saved = reports.saveAndFlush(report);

        // FR-44: snapshot of the resubmitted (current) version.
        reportService.recordVersion(saved);

        storePhotos(saved, submittedViolations);
        return new ReportSubmissionResult(saved.getId(), false);
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private List<ViolationDraft> toDrafts(List<ReportSubmissionCommand.ViolationSubmission> submittedViolations) {
        return submittedViolations.stream()
            .map(violation -> new ViolationDraft(
                violation.briefDescription(),
                violation.observationHazard(),
                violation.observationHazardOther(),
                violation.riskRating(),
                violation.riskRatingOther(),
                violation.actionRecommended(),
                violation.violatorName(),
                violation.violatorBadgeNo(),
                violation.violatorTitle(),
                violation.taskIncharge()))
            .toList();
    }

    /** Writes the photographs of a (re)submission, rolling back written files if storage fails. */
    private void storePhotos(SafetyReport saved,
                             List<ReportSubmissionCommand.ViolationSubmission> submittedViolations) {
        List<Violation> savedViolations = saved.getViolations();
        List<String> writtenFiles = new ArrayList<>();
        try {
            for (int index = 0; index < savedViolations.size() && index < submittedViolations.size(); index++) {
                List<PhotoUpload> uploads = submittedViolations.get(index).photos();
                if (uploads == null) {
                    continue;
                }
                for (PhotoUpload upload : uploads) {
                    writtenFiles.add(photoStore.store(savedViolations.get(index).getId(), upload).getStoredFileName());
                }
            }
        } catch (RuntimeException failure) {
            writtenFiles.forEach(photoStore::deleteStoredFile);
            throw failure;
        }
    }
}
