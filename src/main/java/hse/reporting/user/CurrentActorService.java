package hse.reporting.user;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/** Resolves the acting user from the server-side security context (never from the form). */
@Service
public class CurrentActorService {

    private final InspectorDesignationRepository designations;

    public CurrentActorService(InspectorDesignationRepository designations) {
        this.designations = designations;
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

    /** FR-04 designation of the acting Inspector; {@code null} when not designated. */
    public String managerEmail() {
        return designations.findById(email())
            .map(InspectorDesignation::getManagerEmail)
            .orElse(null);
    }
}
