package com.kolyvanova.chatter.exception;

public class SelfSubscriptionException extends RuntimeException {

    public SelfSubscriptionException() {
        super("error.subscription.self");
    }
}
