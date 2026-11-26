package com.hse.reporting.useraccess.access;

import com.hse.reporting.useraccess.domain.Role;

/**
 * The user on whose behalf a use case runs. Built by the application layer
 * ({@code ActorContext}) from the authenticated session — no module may assume
 * an actor on its own (AGENTS.md section 4.2).
 *
 * @param email canonical (as stored) company email of the acting user
 * @param role  the assigned system role (ADR-03)
 */
public record Actor(String email, Role role) {
}
