package hse.reporting.photo;

/** Photograph storage interface owned by Report Evidence / Photograph Handling. */
public interface PhotoStore {

    PhotographEvidence store(Long violationId, PhotoUpload upload);

    /** Removes a stored file after a failed submission; database rows roll back with the transaction. */
    void deleteStoredFile(String storedFileName);

    /**
     * Reads a stored file back for authorised viewing (FR-14/15/16 detail pages).
     * Implementations must keep the path-traversal guard used when writing.
     */
    byte[] read(String storedFileName);
}
