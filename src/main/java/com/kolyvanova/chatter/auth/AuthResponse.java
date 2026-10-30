package com.kolyvanova.chatter.auth;

public record AuthResponse(
        String accessToken,
        String tokenType
) {
}
