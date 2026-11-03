package com.kolyvanova.chatter.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PostUpdateDto(
        @NotBlank(message = "{post.content.required}")
        @Size(max = 280, message = "{post.content.size}")
        String content,
        @NotNull(message = "{post.category.required}")
        PostCategory category
) {
}
