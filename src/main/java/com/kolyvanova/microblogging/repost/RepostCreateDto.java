package com.kolyvanova.microblogging.repost;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RepostCreateDto(
        @NotNull
        UUID postId
) {
}
