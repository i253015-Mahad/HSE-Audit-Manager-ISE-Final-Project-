package hse.reporting.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * FR-04 company designation: the Manager whose projects an Inspector may report on.
 *
 * <p>Kept in its own slice-owned table so the shared {@code users} table remains
 * owned by User and Access Management (architecture.md section 9). No login or
 * role data is duplicated here.
 */
@Entity
@Table(name = "inspector_designation")
public class InspectorDesignation {

    @Id
    @Column(name = "inspector_email", nullable = false, length = 255)
    private String inspectorEmail;

    @Column(name = "manager_email", length = 255)
    private String managerEmail;

    protected InspectorDesignation() {
        // JPA
    }

    public InspectorDesignation(String inspectorEmail, String managerEmail) {
        this.inspectorEmail = inspectorEmail;
        this.managerEmail = managerEmail;
    }

    public String getInspectorEmail() {
        return inspectorEmail;
    }

    public String getManagerEmail() {
        return managerEmail;
    }
}
