package hse.reporting.user;

import org.springframework.data.jpa.repository.JpaRepository;

/** Company designations used by FR-04 project eligibility and project selection. */
public interface InspectorDesignationRepository extends JpaRepository<InspectorDesignation, String> {
}
