package hse.reporting.photo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Photograph reference and metadata; file bytes are stored outside the database. */
@Entity
@Table(name = "photograph_evidence")
public class PhotographEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Plain foreign key: Photograph Handling does not reach into report internals. */
    @Column(name = "violation_id", nullable = false)
    private Long violationId;

    @Column(name = "stored_file_name", nullable = false, unique = true)
    private String storedFileName;

    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    protected PhotographEvidence() {
    }

    public PhotographEvidence(Long violationId, String storedFileName, String originalFileName,
                              String contentType, long sizeBytes) {
        this.violationId = violationId;
        this.storedFileName = storedFileName;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
    }

    public Long getId() {
        return id;
    }

    public Long getViolationId() {
        return violationId;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }
}
