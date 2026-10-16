package com.kolyvanova.microblogging.comment;

import java.time.Instant;
import java.util.UUID;

public record CommentReadDto(
        UUID id,
        UUID postId,
        UUID authorId,
        String content,
        Instant createdAt
) {
}
