package com.kolyvanova.microblogging.image;

import com.kolyvanova.microblogging.exception.InvalidFileException;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;

@Service
@RequiredArgsConstructor
public class FileService {
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
        try {
            Resource resource = storageService.load(key);
            String contentType = tika.detect(resource.getFile());
            return new ImageResponse(resource, contentType);
        } catch (UncheckedIOException | IOException e) {
            throw new InvalidFileException("Failed to load file: " + e);
        }
    }

    public void delete(String key) {
        storageService.delete(key);
    }
}
