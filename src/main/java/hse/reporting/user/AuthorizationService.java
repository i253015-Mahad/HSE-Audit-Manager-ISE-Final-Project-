package hse.reporting.user;

import hse.reporting.application.ports.AuthorizationPort;
import org.springframework.stereotype.Service;

/** Authorization decisions owned by User and Access Management. */
@Service
public class AuthorizationService implements AuthorizationPort {

    private final UserRepository users;

    public AuthorizationService(UserRepository users) {
        this.users = users;
    }

    @Override
    public boolean canSubmitReport(String userEmail) {
        return users.findByEmail(userEmail)
            .filter(user -> user.isEnabled() && user.getRole() == Role.INSPECTOR)
            .isPresent();
    }
}
