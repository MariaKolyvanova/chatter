package com.kolyvanova.microblogging.like;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LikeCreateDto(
        @NotNull
        UUID postId
) {
}
