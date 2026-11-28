package hse.reporting.report;

/** Submission state is separate from Open/Closed progress (AGENTS.md §7). */
public enum SubmissionStatus {
    PENDING_APPROVAL,
    APPROVED,
    REJECTED
}
