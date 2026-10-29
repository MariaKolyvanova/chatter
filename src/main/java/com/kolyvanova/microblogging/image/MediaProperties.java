package com.kolyvanova.microblogging.image;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "app.media")
public record MediaProperties (
        Path directory,
        long maxBytes
) {
}
