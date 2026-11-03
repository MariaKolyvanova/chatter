package com.kolyvanova.chatter.user;

import java.time.LocalDate;
import java.util.UUID;

public record UserStaffReadDto(
        UUID id,
        String username,
        String displayName,
        String bio,
        String location,
        String avatarKey,
        LocalDate birthDate,
        UserRole role,
        UserStatus status
) {
}
