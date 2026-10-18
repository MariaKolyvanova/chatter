package com.kolyvanova.microblogging.exception;

public class SelfSubscriptionException extends RuntimeException {

    public SelfSubscriptionException() {
        super("A user cannot subscribe to themselves");
    }
}
