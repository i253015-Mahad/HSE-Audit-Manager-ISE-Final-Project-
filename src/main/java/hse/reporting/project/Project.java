package hse.reporting.project;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Project aggregate root — owned by the Project Management module
 * (architecture §5.3.B; ADR-02).
 *
 * <p>Business rules encoded here or enforced by {@link ProjectService}:
 * <ul>
 *   <li>Project names are UNIQUE across the system (PRD FR-31, EC-36).</li>
 *   <li>Exactly ONE Manager email at a time — assigned after creation,
 *       reassigned while the project is open (PRD FR-31, EC-10, BR-15).</li>
 *   <li>State is OPEN until an Admin closes it; closure is confirmed and,
 *       on confirm, auto-closes Open reports and discards Pending ones
 *       (PRD FR-32…FR-35, flow 9.4).</li>
 *   <li>{@code closedAt} records the EXACT closure timestamp and starts the
 *       3-month retention window for purge-all (PRD NFR-03, FR-39).</li>
 * </ul>
 */
@Entity
@Table(name = "projects")
public class Project {

    public enum State { OPEN, CLOSED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String name;

    /** Manager email; {@code null} until assigned after creation (FR-31). */
    @Column(length = 200)
    private String managerEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private State state = State.OPEN;

    /** Exact closure timestamp; start of the 3-month retention window (NFR-03). */
    private LocalDateTime closedAt;

    protected Project() {
        // JPA
    }

    public Project(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    /** Test seam for in-memory store fakes (package-private — not part of the API). */
    void setIdForTest(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManagerEmail() {
        return managerEmail;
    }

    public void setManagerEmail(String managerEmail) {
        this.managerEmail = managerEmail;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public boolean isOpen() {
        return state == State.OPEN;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }
}
