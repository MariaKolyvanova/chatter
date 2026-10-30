package com.kolyvanova.microblogging.image;

import com.kolyvanova.microblogging.exception.InvalidFileException;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;

@Service
@RequiredArgsConstructor
public class FileService {
    public static final String DEFAULT_AVATAR_KEY = "avatar/default.png";

    private final ImageValidator imageValidator;
    private final LocalStorageService storageService;
    private final Tika tika;

    public String upload(String folder, MultipartFile file) {
        try {
            byte[] content = file.getBytes();
            ValidatedFile validatedFile = imageValidator.validate(content);
            String key = storageService.save(folder, content, validatedFile.extension());

            return key;
        } catch (IOException | UncheckedIOException e) {
            throw new InvalidFileException("Failed to save file: " + e);
        }
    }

    public ImageResponse load(String key) {
        if (DEFAULT_AVATAR_KEY.equals(key)) {
            return loadDefault();
        }

        try {
            Resource resource = storageService.load(key);
            String contentType = tika.detect(resource.getFile());
            return new ImageResponse(resource, contentType);
        } catch (UncheckedIOException | IOException e) {
            throw new InvalidFileException("Failed to load file: " + e);
        }
    }

    private ImageResponse loadDefault() {
        Resource resource = new ClassPathResource(DEFAULT_AVATAR_KEY);
        return new ImageResponse(resource, "image/png");
    }

    public void delete(String key) {
        storageService.delete(key);
    }
}
