package hse.reporting.photo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Read and cleanup surface for photograph evidence, so the application layer
 * never talks to {@link PhotographEvidenceRepository} or the file store directly
 * (AGENTS.md section 6: callers reach this capability through an interface).
 */
@Service
public class PhotoEvidenceQuery {

    private final PhotographEvidenceRepository evidence;
    private final PhotoStore photoStore;

    public PhotoEvidenceQuery(PhotographEvidenceRepository evidence, PhotoStore photoStore) {
        this.evidence = evidence;
        this.photoStore = photoStore;
    }

    /** Photographs attached to the given violations, for a report detail or edit page. */
    @Transactional(readOnly = true)
    public List<PhotographEvidence> forViolations(Collection<Long> violationIds) {
        if (violationIds == null || violationIds.isEmpty()) {
            return List.of();
        }
        return evidence.findByViolationIdInOrderByOriginalFileNameAsc(violationIds);
    }

    /** One photograph, but only when it belongs to one of the given violations. */
    @Transactional(readOnly = true)
    public Optional<PhotographEvidence> findOwnedBy(Long photographId, Collection<Long> violationIds) {
        if (violationIds == null || violationIds.isEmpty()) {
            return Optional.empty();
        }
        for (Long violationId : violationIds) {
            PhotographEvidence found = evidence.findByIdAndViolationId(photographId, violationId);
            if (found != null) {
                return Optional.of(found);
            }
        }
        return Optional.empty();
    }

    /** Bytes for streaming to the browser; the content type comes from the metadata row. */
    public byte[] bytesOf(PhotographEvidence photograph) {
        return photoStore.read(photograph.getStoredFileName());
    }

    /** How many photographs a violation already carries — used to hold the 3-per-violation cap on edit. */
    @Transactional(readOnly = true)
    public long countFor(Long violationId) {
        return evidence.countByViolationId(violationId);
    }

    /**
     * Removes every photograph of the given violations, rows and files together.
     *
     * <p>Called by the resubmit flow for violations the Inspector deleted: the
     * rows would otherwise point at violations that no longer exist, and the
     * files would never be reclaimed.
     */
    @Transactional
    public void deleteForViolations(Collection<Long> violationIds) {
        if (violationIds == null || violationIds.isEmpty()) {
            return;
        }
        List<PhotographEvidence> doomed = evidence.findByViolationIdIn(violationIds);
        for (PhotographEvidence photograph : doomed) {
            photoStore.deleteStoredFile(photograph.getStoredFileName());
        }
        evidence.deleteByViolationIdIn(violationIds);
    }
}
