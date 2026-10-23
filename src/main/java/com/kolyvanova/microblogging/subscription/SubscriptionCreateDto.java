package com.kolyvanova.microblogging.subscription;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SubscriptionCreateDto(
        @NotNull
        UUID authorId
) {
}
