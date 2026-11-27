package hse.reporting.application.report;

/** duplicateSubmission is true when a repeated POST reused an already claimed token. */
public record ReportSubmissionResult(Long reportId, boolean duplicateSubmission) {
}
