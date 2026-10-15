package com.kolyvanova.microblogging.user;

import java.time.LocalDate;
import java.util.UUID;

public record UserReadDto (
    UUID id,
    String username,
    String email,
    String displayName,
    String bio,
    UserRole role,
    String location,
    LocalDate birthDate,
    UserStatus status
) {

}