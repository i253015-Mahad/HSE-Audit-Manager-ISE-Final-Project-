package hse.reporting.photo;

/** Transport-neutral photograph input; web-layer MultipartFile is mapped to this. */
public record PhotoUpload(String originalFilename, String contentType, byte[] content) {
}
