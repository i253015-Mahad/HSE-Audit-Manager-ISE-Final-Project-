package hse.reporting.report;

/** Immutable observer identity snapshotted onto a submitted SOR. */
public record ObserverDetails(String email, String name, String badgeNumber, String title, String company) {
}
