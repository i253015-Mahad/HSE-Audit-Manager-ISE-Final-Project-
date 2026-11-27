package hse.reporting.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** User and Access Management data (A-04: company designation of Inspector's Manager). */
@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /** The Manager this Inspector belongs to (company designation data, A-04). */
    @Column(name = "manager_email")
    private String managerEmail;

    @Column(nullable = false)
    private boolean enabled = true;

    protected AppUser() {
    }

    public AppUser(String email, String passwordHash, Role role, String managerEmail) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.managerEmail = managerEmail;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public String getManagerEmail() {
        return managerEmail;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
