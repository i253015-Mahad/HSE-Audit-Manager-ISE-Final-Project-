package hse.reporting.photo;

/** Photograph storage interface owned by Report Evidence / Photograph Handling. */
public interface PhotoStore {

    PhotographEvidence store(Long violationId, PhotoUpload upload);

    /** Removes a stored file after a failed submission; database rows roll back with the transaction. */
    void deleteStoredFile(String storedFileName);
}
