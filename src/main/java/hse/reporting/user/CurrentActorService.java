package hse.reporting.user;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/** Resolves the acting user from the server-side security context (never from the form). */
@Service
public class CurrentActorService {

    private final UserRepository users;

    public CurrentActorService(UserRepository users) {
        this.users = users;
    }

    public String email() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
            || !authentication.isAuthenticated()
            || authentication.getPrincipal() == null
            || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Your session has expired. Sign in again before submitting.");
        }
        return authentication.getName();
    }

    public AppUser currentUser() {
        String email = email();
        return users.findByEmail(email)
            .orElseThrow(() -> new AccessDeniedException("Your session is no longer valid. Sign in again."));
    }
}
