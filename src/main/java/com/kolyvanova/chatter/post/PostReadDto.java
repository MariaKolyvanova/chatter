package com.kolyvanova.chatter.post;

import java.time.Instant;
import java.util.UUID;

public record PostReadDto(
        UUID id,
        UUID authorId,
        UUID replyToId,
        UUID conversationId,
        String content,
        PostCategory category,
        PostStatus status,
        long viewCount,
        String imageKey,
        Instant createdAt,
        boolean changed
) {
}
