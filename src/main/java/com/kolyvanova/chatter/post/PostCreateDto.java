package com.kolyvanova.chatter.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record PostCreateDto(
        @NotBlank(message = "{post.content.required}")
        @Size(max = 280, message = "{post.content.size}")
        String content,

        PostCategory category,

        UUID replyToId
) {
}
