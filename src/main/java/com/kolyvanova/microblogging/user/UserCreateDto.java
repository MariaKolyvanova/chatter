package com.kolyvanova.microblogging.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserCreateDto(
        @NotBlank
        @Size(max = 50)
        String username,

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @NotBlank
        @Size(min = 8, max = 100)
        String password,

        @NotBlank
        @Size(max = 100)
        String displayName,

        String bio,

        @Size(max = 100)
        String location,

        @Past
        LocalDate birthDate
) {
}
