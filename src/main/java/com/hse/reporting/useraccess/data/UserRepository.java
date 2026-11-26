package com.hse.reporting.useraccess.data;

import com.hse.reporting.useraccess.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence for {@link User}. Only the User and Access Management module uses
 * this repository; other modules ask that module through its interfaces (AGENTS.md section 6).
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /** Company emails are matched case-insensitively. */
    Optional<User> findByEmailIgnoreCase(String email);
}
