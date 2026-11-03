package com.kolyvanova.chatter.image;

import com.kolyvanova.chatter.exception.InvalidFileException;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

@Service
@RequiredArgsConstructor
public class ImageValidator {

    private final Tika tika;
    private final MediaProperties mediaProperties;

    public ValidatedFile validate(byte[] content){
        if (content == null || content.length == 0){
            throw new InvalidFileException("error.file.empty");
        }
        if (content.length > mediaProperties.maxBytes()) {
            throw new InvalidFileException("error.file.too-large");
        }

        String contentType = tika.detect(content);

        String extension = switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/heif" -> ".heif";
            default -> throw new InvalidFileException("error.file.unsupported", contentType);
        };

        return new ValidatedFile(contentType, extension);
    }


}