package hse.reporting.photo;

import hse.reporting.common.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.util.List;

/** Team decision (27 Nov 2026): max 3 per violation, 10 MB each, JPEG or PNG only. */
@Component
public class PhotoValidator {

    public static final int MAX_PHOTOS_PER_VIOLATION = 3;
    public static final long MAX_FILE_SIZE_BYTES = 10L * 1024L * 1024L;

    public void validate(List<PhotoUpload> uploads) {
        if (uploads == null || uploads.isEmpty()) {
            return;
        }
        if (uploads.size() > MAX_PHOTOS_PER_VIOLATION) {
            throw new BusinessRuleException("A violation may have at most " + MAX_PHOTOS_PER_VIOLATION + " photographs.");
        }
        for (PhotoUpload upload : uploads) {
            formatOf(upload);
        }
    }

    /** Returns the detected format or throws with a user-safe refusal message. */
    public PhotoFormat formatOf(PhotoUpload upload) {
        if (upload == null || upload.content() == null || upload.content().length == 0) {
            throw new BusinessRuleException("A photograph file is empty.");
        }
        if (upload.content().length > MAX_FILE_SIZE_BYTES) {
            throw new BusinessRuleException("Each photograph must be 10 MB or smaller.");
        }

        String contentType = upload.contentType() == null ? "" : upload.contentType().trim().toLowerCase();
        PhotoFormat expected = switch (contentType) {
            case "image/jpeg", "image/jpg" -> PhotoFormat.JPEG;
            case "image/png" -> PhotoFormat.PNG;
            default -> throw new BusinessRuleException("Only JPEG or PNG photographs are accepted.");
        };
        if (!expected.matches(upload.content())) {
            throw new BusinessRuleException("Photograph content does not match its file type.");
        }
        return expected;
    }
}
