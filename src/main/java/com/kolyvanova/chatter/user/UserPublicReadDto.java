package com.kolyvanova.chatter.user;

import java.util.UUID;

public record UserPublicReadDto(
        UUID id,
        String username,
        String displayName,
        String bio,
        String location,
        String avatarKey
) {
}
