package com.kolyvanova.microblogging.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostCreateDto(
        @NotBlank
        String content,

        @NotNull
        PostCategory category
) {
}
