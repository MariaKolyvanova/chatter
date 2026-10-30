package com.kolyvanova.chatter.subscription;

import java.time.Instant;
import java.util.UUID;

public record SubscriptionReadDto(
        UUID id,
        UUID subscriberId,
        UUID authorId,
        Instant createdAt
) {
}
