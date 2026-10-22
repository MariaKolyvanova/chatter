package com.kolyvanova.microblogging.auth;

public record AuthResponse(
        String accessToken,
        String tokenType
) {
}
