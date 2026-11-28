package hse.reporting.application.ports;

/** Project state and Manager context owned by Project Management (FR-04). */
public interface ProjectEligibilityPort {

    Eligibility check(String inspectorEmail, Long projectId);

    record Eligibility(boolean allowed, String refusalMessage, String managerEmail) {

        public static Eligibility permit(String managerEmail) {
            return new Eligibility(true, null, managerEmail);
        }

        public static Eligibility refuse(String message) {
            return new Eligibility(false, message, null);
        }
    }
}
