package com.kolyvanova.chatter.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserUpdateDto(
        @NotBlank(message = "{user.username.required}")
        @Size(min = 3, max = 50, message = "{user.username.size}")
        @Pattern(regexp = "^[A-Za-z0-9_]{3,50}$", message = "{user.username.invalid}")
        String username,

        @NotBlank(message = "{user.email.required}")
        @Email(message = "{user.email.invalid}")
        @Size(max = 255, message = "{user.email.size}")
        String email,

        @NotBlank(message = "{user.display-name.required}")
        @Size(min = 3, max = 100, message = "{user.display-name.size}")
        String displayName,

        String bio,

        @Size(max = 100, message = "{user.location.size}")
        String location,

        @Past(message = "{user.birth-date.past}")
        LocalDate birthDate
) {
}
