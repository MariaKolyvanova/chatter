package com.kolyvanova.microblogging.image;

import org.springframework.core.io.Resource;

public record ImageResponse (
        Resource resource,
        String contentType
) {
}
