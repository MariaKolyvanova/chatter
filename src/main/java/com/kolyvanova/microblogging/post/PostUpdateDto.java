package com.kolyvanova.microblogging.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PostUpdateDto(
        @NotBlank
        @Size(max = 280)
        String content,
        @NotNull
        PostCategory category
) {
}
