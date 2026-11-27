package hse.reporting.application.ports;

/** Authorization decision owned by User and Access Management (AGENTS.md §5.1). */
public interface AuthorizationPort {

    boolean canSubmitReport(String userEmail);
}
