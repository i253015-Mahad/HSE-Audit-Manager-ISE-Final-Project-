package hse.reporting.photo;

import hse.reporting.common.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PhotoValidatorTest {

    private final PhotoValidator validator = new PhotoValidator();

    @Test
    void acceptsJpegAndPng() {
        assertDoesNotThrow(() -> validator.validate(List.of(jpeg(), png())));
        assertEquals(PhotoFormat.JPEG, validator.formatOf(jpeg()));
        assertEquals(PhotoFormat.PNG, validator.formatOf(png()));
    }

    @Test
    void allowsNoPhotographs() {
        assertDoesNotThrow(() -> validator.validate(List.of()));
    }

    @Test
    void rejectsMoreThanThreePhotographs() {
        assertThrows(BusinessRuleException.class,
            () -> validator.validate(List.of(jpeg(), jpeg(), jpeg(), jpeg())));
    }

    @Test
    void rejectsFileLargerThanTenMegabytes() {
        byte[] large = new byte[(int) PhotoValidator.MAX_FILE_SIZE_BYTES + 1];
        large[0] = (byte) 0xFF;
        large[1] = (byte) 0xD8;
        large[2] = (byte) 0xFF;

        assertThrows(BusinessRuleException.class,
            () -> validator.validate(List.of(new PhotoUpload("big.jpg", "image/jpeg", large))));
    }

    @Test
    void rejectsNonPhotographContentType() {
        assertThrows(BusinessRuleException.class,
            () -> validator.validate(List.of(new PhotoUpload("notes.pdf", "application/pdf", jpeg().content()))));
    }

    @Test
    void rejectsSpoofedPhotographContent() {
        PhotoUpload spoofed = new PhotoUpload("fake.png", "image/png", jpeg().content());
        assertThrows(BusinessRuleException.class, () -> validator.validate(List.of(spoofed)));
    }

    private PhotoUpload jpeg() {
        return new PhotoUpload("photo.jpg", "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0});
    }

    private PhotoUpload png() {
        return new PhotoUpload("photo.png", "image/png", new byte[]{
            (byte) 0x89, (byte) 'P', (byte) 'N', (byte) 'G',
            (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A, 0});
    }
}
