package com.hse.reporting.useraccess.data;

import com.hse.reporting.useraccess.access.ManagerDirectory;
import com.hse.reporting.useraccess.domain.Role;
import com.hse.reporting.useraccess.domain.User;
import org.springframework.stereotype.Service;

/** JPA-backed {@link ManagerDirectory} — one indexed lookup on the unique email. */
@Service
public class JpaManagerDirectory implements ManagerDirectory {

    private final UserRepository users;

    public JpaManagerDirectory(UserRepository users) {
        this.users = users;
    }

    @Override
    public boolean isManager(String email) {
        return users.findByEmailIgnoreCase(email.trim())
                .filter(u -> u.getRole() == Role.MANAGER)
                .isPresent();
    }
}
