package hse.reporting.project;

/**
 * Application-layer port through which the Project Management module asks the
 * Safety Report Management module (Eman — Slices 3/4) about the reports of a
 * project when closing it (PRD flow 9.4; AGENTS.md §6.8: propose the interface
 * first, implement behind it).
 *
 * <p>Project Management must NOT read or write report records directly
 * (ADR-02 — capabilities own their data). Until Eman's module lands, the
 * {@link #DEFAULT} implementation returns 0 / does nothing so that project
 * closure still works, but the Flow 9.4 warnings show zero counts.
 *
 * <p>TODO(EMAN / Slices 3–4): provide a real Spring bean implementing this
 * interface, backed by the Safety Report Management module's own queries —
 * do not let Project Management query the reports table itself.
 */
public interface ProjectReportSummary {

    /** Number of reports in state Open for the project (FR-32 warning input). */
    long countOpen(Long projectId);

    /** Number of reports in state Pending Approval for the project (FR-35 warning input). */
    long countPending(Long projectId);

    /** Auto-close ALL Open reports of the project (FR-33, called on confirmed closure). */
    void closeAllOpen(Long projectId);

    /** Discard ALL Pending Approval reports of the project (FR-35, on confirmed closure). */
    void discardPending(Long projectId);

    /**
     * Interim no-op wiring — loud placeholder so integration status is visible.
     * Replace with Eman's real implementation as soon as it exists.
     */
    ProjectReportSummary DEFAULT = new ProjectReportSummary() {
        @Override
        public long countOpen(Long projectId) {
            // TODO(EMAN): real count — closure warnings show 0 until then.
            return 0L;
        }

        @Override
        public long countPending(Long projectId) {
            // TODO(EMAN): real count — closure warnings show 0 until then.
            return 0L;
        }

        @Override
        public void closeAllOpen(Long projectId) {
            // TODO(EMAN): real side-effect — Open reports stay Open until then.
        }

        @Override
        public void discardPending(Long projectId) {
            // TODO(EMAN): real side-effect — Pending reports stay Pending until then.
        }
    };
}
