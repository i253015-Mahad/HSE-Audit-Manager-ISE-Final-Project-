package hse.reporting.photo;

/** Photograph-only evidence (ADR-06): JPEG and PNG, verified by file signature. */
public enum PhotoFormat {
    JPEG("jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
    PNG("png", new byte[]{(byte) 0x89, 'P', 'N', 'G', (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A});

    private final String extension;
    private final byte[] signature;

    PhotoFormat(String extension, byte[] signature) {
        this.extension = extension;
        this.signature = signature;
    }

    public String extension() {
        return extension;
    }

    public boolean matches(byte[] content) {
        if (content == null || content.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (content[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }
}
