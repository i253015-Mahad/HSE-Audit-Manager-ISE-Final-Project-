package com.hse.reporting.useraccess.access;

/**
 * Read-only directory query published by User and Access Management so other
 * capabilities can verify a submitted Manager email against the company account
 * list (PRD BR-19: accounts are decided by the company before use). Consumers
 * never touch the {@code users} table directly (AGENTS.md section 6).
 */
public interface ManagerDirectory {

    /** True when the email matches an existing account whose role is MANAGER. */
    boolean isManager(String email);
}
