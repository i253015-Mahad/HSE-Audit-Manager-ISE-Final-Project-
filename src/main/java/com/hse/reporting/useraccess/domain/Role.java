package com.hse.reporting.useraccess.domain;

/**
 * The three system roles of the HSE Safety Reporting System (PRD section 4).
 *
 * <p>The role is stored with the user record (ADR-03: "each user has an assigned
 * system role"); project-level scope is evaluated separately from the Manager
 * project assignment, see {@code AuthorizationService#canAccessProject}.
 */
public enum Role {

    /** Safety Team Inspector — creates and submits safety reports. */
    INSPECTOR,

    /** Manager — reviews reports of the projects assigned to them. */
    MANAGER,

    /** HSE Engineer/Admin — cross-project oversight and administration. */
    ADMIN;

    /**
     * Spring Security authority for this role, e.g. {@code ROLE_MANAGER}.
     */
    public String authority() {
        return "ROLE_" + name();
    }

    /**
     * Resolves a Spring Security authority back to a system role.
     *
     * @param authority an authority such as {@code ROLE_MANAGER}
     * @return the matching role, or {@code null} if the authority is not a system role
     */
    public static Role fromAuthority(String authority) {
        if (authority == null || !authority.startsWith("ROLE_")) {
            return null;
        }
        String name = authority.substring("ROLE_".length());
        for (Role role : values()) {
            if (role.name().equals(name)) {
                return role;
            }
        }
        return null;
    }
}
