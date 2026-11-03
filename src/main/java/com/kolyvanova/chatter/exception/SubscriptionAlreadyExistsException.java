package com.kolyvanova.chatter.exception;

public class SubscriptionAlreadyExistsException extends RuntimeException {

    public SubscriptionAlreadyExistsException() {
        super("error.subscription.already-exists");
    }
}
