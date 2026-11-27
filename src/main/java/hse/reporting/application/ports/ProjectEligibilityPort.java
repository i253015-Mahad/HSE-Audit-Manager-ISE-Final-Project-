package hse.reporting.application.ports;

/** Project state and Manager context owned by Project Management (FR-04). */
public interface ProjectEligibilityPort {

    Eligibility check(String inspectorEmail, Long projectId);

    record Eligibility(boolean allowed, String refusalMessage) {

        public static Eligibility permit() {
            return new Eligibility(true, null);
        }

        public static Eligibility refuse(String message) {
            return new Eligibility(false, message);
        }
    }
}
