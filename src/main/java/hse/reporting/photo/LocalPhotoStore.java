package hse.reporting.photo;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

/** ADR-06: files on the local filesystem, references and metadata in the database. */
@Service
public class LocalPhotoStore implements PhotoStore {

    private final Path directory;
    private final PhotoValidator validator;
    private final PhotographEvidenceRepository evidence;

    public LocalPhotoStore(@Value("${hse.photos.directory}") String directory,
                           PhotoValidator validator,
                           PhotographEvidenceRepository evidence) {
        this.directory = Paths.get(directory).toAbsolutePath().normalize();
        this.validator = validator;
        this.evidence = evidence;
    }

    @PostConstruct
    public void createDirectory() throws IOException {
        Files.createDirectories(directory);
    }

    @Override
    public PhotographEvidence store(Long violationId, PhotoUpload upload) {
        PhotoFormat format = validator.formatOf(upload);
        String storedFileName = UUID.randomUUID() + "." + format.extension();
        Path target = resolveInsideStorage(storedFileName);
        try {
            Files.write(target, upload.content(),
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not store photograph evidence", exception);
        }
        return evidence.save(new PhotographEvidence(violationId, storedFileName,
            upload.originalFilename(), upload.contentType(), upload.content().length));
    }

    @Override
    public void deleteStoredFile(String storedFileName) {
        Path target = resolveInsideStorage(storedFileName);
        try {
            Files.deleteIfExists(target);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not remove photograph evidence", exception);
        }
    }

    private Path resolveInsideStorage(String fileName) {
        Path target = directory.resolve(fileName).normalize();
        if (!directory.equals(target.getParent())) {
            throw new IllegalArgumentException("Invalid photograph file name");
        }
        return target;
    }
}
