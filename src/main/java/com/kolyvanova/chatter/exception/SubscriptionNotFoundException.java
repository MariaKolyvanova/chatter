package com.kolyvanova.chatter.exception;

public class SubscriptionNotFoundException extends RuntimeException {

    public SubscriptionNotFoundException() {
        super("error.subscription.not-found");
    }
}
