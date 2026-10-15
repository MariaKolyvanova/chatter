package com.kolyvanova.microblogging.post;

import java.time.Instant;
import java.util.UUID;

public record PostReadDto(
        UUID id,
        UUID authorId,
        String content,
        PostCategory category,
        PostStatus status,
        Instant createdAt
) {
}
