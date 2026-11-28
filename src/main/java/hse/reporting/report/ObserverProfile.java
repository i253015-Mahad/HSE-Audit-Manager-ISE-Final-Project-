package hse.reporting.report;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * SOR-owned observer profile keyed by the Inspector's company email.
 * Observer identity is auto-filled from this profile at form render and
 * snapshotted onto the report at submission (FR-03).
 */
@Entity
@Table(name = "observer_profile")
public class ObserverProfile {

    @Id
    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "badge_no", nullable = false)
    private String badgeNumber;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "company", nullable = false)
    private String company;

    protected ObserverProfile() {
    }

    public ObserverProfile(String email, String fullName, String badgeNumber, String title, String company) {
        this.email = email;
        this.fullName = fullName;
        this.badgeNumber = badgeNumber;
        this.title = title;
        this.company = company;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getBadgeNumber() {
        return badgeNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getCompany() {
        return company;
    }
}
