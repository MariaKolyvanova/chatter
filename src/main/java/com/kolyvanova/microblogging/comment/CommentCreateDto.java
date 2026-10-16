package com.kolyvanova.microblogging.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CommentCreateDto(

        @NotNull
        UUID postId,

        @NotNull
        UUID authorId,

        @NotBlank
        String content
) {
}
