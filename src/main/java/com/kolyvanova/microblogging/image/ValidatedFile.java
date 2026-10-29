package com.kolyvanova.microblogging.image;

public record ValidatedFile(
        String contentType,
        String extension
) {
}
