package hse.reporting.user;

import com.hse.reporting.application.ActorContext;
import com.hse.reporting.useraccess.access.AuthorizationService;
import com.hse.reporting.useraccess.domain.Role;
import hse.reporting.application.ports.AuthorizationPort;
import org.springframework.stereotype.Service;

/**
 * Bridges the report slice's authorization port to the single policy bean owned by
 * User and Access Management (ADR-03 / ASR-01). No second access policy is defined.
 */
@Service
public class ReportAuthorizationAdapter implements AuthorizationPort {

    private final ActorContext actors;
    private final AuthorizationService authorization;

    public ReportAuthorizationAdapter(ActorContext actors, AuthorizationService authorization) {
        this.actors = actors;
        this.authorization = authorization;
    }

    @Override
    public boolean canSubmitReport(String userEmail) {
        return authorization.hasRole(actors.currentActor(), Role.INSPECTOR);
    }
}
