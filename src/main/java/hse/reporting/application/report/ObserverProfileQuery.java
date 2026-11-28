package hse.reporting.application.report;

import hse.reporting.common.BusinessRuleException;
import hse.reporting.report.ObserverDetails;
import hse.reporting.report.ObserverProfile;
import hse.reporting.report.ObserverProfileRepository;
import hse.reporting.user.CurrentActorService;
import org.springframework.stereotype.Service;

/** Resolves the signed-in Inspector's observer profile; never trusts the form. */
@Service
public class ObserverProfileQuery {

    private final ObserverProfileRepository profiles;
    private final CurrentActorService currentActor;

    public ObserverProfileQuery(ObserverProfileRepository profiles, CurrentActorService currentActor) {
        this.profiles = profiles;
        this.currentActor = currentActor;
    }

    /** Null when the Inspector has not been provisioned; used for form display. */
    public ObserverDetails currentObserver() {
        return profiles.findById(currentActor.email()).map(this::toDetails).orElse(null);
    }

    /** Submission must fail clearly when the observer profile is missing. */
    public ObserverDetails requireCurrentObserver() {
        ObserverDetails observer = currentObserver();
        if (observer == null) {
            throw new BusinessRuleException(
                "Your SOR observer profile is not provisioned. Contact your HSE administrator.");
        }
        return observer;
    }

    private ObserverDetails toDetails(ObserverProfile profile) {
        return new ObserverDetails(profile.getEmail(), profile.getFullName(), profile.getBadgeNumber(),
            profile.getTitle(), profile.getCompany());
    }
}
