package com.kolyvanova.microblogging.comment;

import jakarta.validation.constraints.NotBlank;

public record CommentUpdateDto (
        @NotBlank
        String content
) {

}
