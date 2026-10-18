package com.kolyvanova.microblogging.exception;

public class SubscriptionAlreadyExistsException extends RuntimeException {

    public SubscriptionAlreadyExistsException() {
        super("Subscription already exists");
    }
}
