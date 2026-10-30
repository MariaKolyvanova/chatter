package com.kolyvanova.chatter.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank
        String username,

        @NotBlank
        String password
) {
}
