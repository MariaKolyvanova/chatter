package com.kolyvanova.chatter.exception;

import java.util.UUID;

public class SubscriptionNotFoundException extends RuntimeException {

    public SubscriptionNotFoundException(UUID subscriberId, UUID authorId) {
        super("Subscription to: " + authorId + " not found: " + subscriberId);
    }

    public SubscriptionNotFoundException(UUID id) {
        super("Subscription not found: " + id);
    }
}
