package com.kolyvanova.chatter.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record PostCreateDto(
        @NotBlank
        @Size(max = 280)
        String content,

        PostCategory category,

        UUID replyToId
) {
}
