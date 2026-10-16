package com.kolyvanova.microblogging.like;

import java.time.Instant;
import java.util.UUID;

public record LikeReadDto(
        UUID id,
        UUID postId,
        UUID userId,
        Instant createdAt
) {
}
