package com.hse.reporting.useraccess.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A system user (User and Access Management owns this data, architecture.md section 9).
 *
 * <p>Identity is the company-issued email (NFR-01). Passwords are stored only as
 * hashes; account creation, password reset and lockout are handled outside the
 * software by Account Administration (PRD section 2.2).
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Company-issued email; doubles as the login name. */
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    /** BCrypt hash of the password. The plain password never reaches this class. */
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    /** The user's assigned system role (ADR-03). */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    protected User() {
        // for JPA
    }

    public User(String email, String passwordHash, Role role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
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
}
