package com.kolyvanova.chatter.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordDto(
        @NotBlank(message = "{user.password.current-required}")
        String currentPassword,

        @NotBlank(message = "{user.password.new-required}")
        @Size(min = 8, max = 100, message = "{user.password.new-size}")
        String newPassword
) {
}
