package com.kolyvanova.chatter.image;

import com.kolyvanova.chatter.exception.FileNotFoundException;
import com.kolyvanova.chatter.exception.ImageNotFoundException;
import com.kolyvanova.chatter.exception.InvalidFileException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static com.kolyvanova.chatter.image.FileService.DEFAULT_AVATAR_KEY;

@Service
@RequiredArgsConstructor
public class LocalStorageService {

    private final MediaProperties mediaProperties;

    public String save(String folder, byte[] content, String extension) {
        String key = folder + "/" + UUID.randomUUID() + extension;
        Path path = resolve(key);

        try {
            Files.createDirectories(path.getParent());
            Files.write(path, content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return key;
    }

    public Resource load(String key) {
        Path path = resolve(key);

        if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
            throw new ImageNotFoundException(key);
        }

        return new FileSystemResource(path);
    }

    public void delete(String key) {
        if (key == null || DEFAULT_AVATAR_KEY.equals(key)) {
            return;
        }

        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException e) {
            throw new FileNotFoundException(key);
        }

    }

    private Path resolve(String key) {
        Path root = mediaProperties.directory()
                .toAbsolutePath()
                .normalize();

        Path path = root.resolve(key)
                .normalize();

        if (!path.startsWith(root)) {
            throw new InvalidFileException(String.format("Path '%s' does not start with '%s'", path, root));
        };

        return path;
    }
}
