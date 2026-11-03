package com.kolyvanova.chatter.image;

import com.kolyvanova.chatter.exception.InvalidFileException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;

@Slf4j
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
            log.warn("Failed to save a file in {}", folder);
            throw new InvalidFileException("error.file.save");
        }
    }

    public ImageResponse load(String key) {
        try {
            Resource resource = storageService.load(key);
            String contentType = tika.detect(resource.getFile());
            return new ImageResponse(resource, contentType);
        } catch (UncheckedIOException | IOException e) {
            log.warn("Failed to load file {}", key);
            throw new InvalidFileException("error.file.load");
        }
    }

    public void delete(String key) {
        storageService.delete(key);
    }
}
