package com.kolyvanova.chatter.exception;

public class SelfSubscriptionException extends RuntimeException {

    public SelfSubscriptionException() {
        super("A user cannot subscribe to themselves");
    }
}
