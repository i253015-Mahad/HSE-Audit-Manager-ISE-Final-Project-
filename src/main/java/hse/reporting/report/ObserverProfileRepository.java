package hse.reporting.report;

import org.springframework.data.jpa.repository.JpaRepository;

/** Observer profiles provisioned for Inspectors who create SORs. */
public interface ObserverProfileRepository extends JpaRepository<ObserverProfile, String> {
}
