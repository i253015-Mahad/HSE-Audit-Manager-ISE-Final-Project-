package hse.reporting.application.report;

import hse.reporting.application.ports.AuthorizationPort;
import hse.reporting.application.ports.ProjectEligibilityPort;
import hse.reporting.common.BusinessRuleException;
import hse.reporting.photo.PhotoStore;
import hse.reporting.photo.PhotoUpload;
import hse.reporting.photo.PhotoValidator;
import hse.reporting.report.ObserverDetails;
import hse.reporting.report.SafetyReport;
import hse.reporting.report.SafetyReportRepository;
import hse.reporting.report.SafetyReportService;
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

/** Coordinates FR-01…FR-08 across the owning modules in one transaction. */
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
    private final SubmissionTokenRepository tokens;

    public ReportSubmissionUseCase(AuthorizationPort authorization,
                                   ProjectEligibilityPort projectEligibility,
                                   CurrentActorService currentActor,
                                   ObserverProfileQuery observers,
                                   SafetyReportService reportService,
                                   SafetyReportRepository reports,
                                   PhotoValidator photoValidator,
                                   PhotoStore photoStore,
                                   SubmissionTokenRepository tokens) {
        this.authorization = authorization;
        this.projectEligibility = projectEligibility;
        this.currentActor = currentActor;
        this.observers = observers;
        this.reportService = reportService;
        this.reports = reports;
        this.photoValidator = photoValidator;
        this.photoStore = photoStore;
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
        List<ViolationDraft> drafts = submittedViolations.stream()
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

        List<String> writtenFiles = new ArrayList<>();
        try {
            List<Violation> savedViolations = saved.getViolations();
            for (int i = 0; i < savedViolations.size(); i++) {
                List<PhotoUpload> photos = submittedViolations.get(i).photos();
                if (photos == null) {
                    continue;
                }
                for (PhotoUpload photo : photos) {
                    writtenFiles.add(photoStore.store(savedViolations.get(i).getId(), photo).getStoredFileName());
                }
            }
            reportService.recordVersion(saved);
            token.markUsed(saved.getId());
            return new ReportSubmissionResult(saved.getId(), false);
        } catch (RuntimeException failure) {
            writtenFiles.forEach(photoStore::deleteStoredFile);
            throw failure;
        }
    }
}
