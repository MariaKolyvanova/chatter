package com.kolyvanova.chatter.repost;

import java.time.Instant;
import java.util.UUID;

public record RepostReadDto(
        UUID id,
        UUID postId,
        UUID userId,
        Instant createdAt
) {
}
