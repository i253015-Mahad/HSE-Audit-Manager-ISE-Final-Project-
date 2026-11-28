package hse.reporting.photo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PhotographEvidenceRepository extends JpaRepository<PhotographEvidence, Long> {

    /** Photographs of the given violations, ordered so thumbnails render consistently. */
    List<PhotographEvidence> findByViolationIdInOrderByOriginalFileNameAsc(Collection<Long> violationIds);

    PhotographEvidence findByIdAndViolationId(Long id, Long violationId);

    long countByViolationId(Long violationId);

    List<PhotographEvidence> findByViolationIdIn(Collection<Long> violationIds);

    void deleteByViolationIdIn(Collection<Long> violationIds);
}
