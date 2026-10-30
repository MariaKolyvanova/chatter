package com.kolyvanova.chatter.exception;

public class TopicSubscriptionAlreadyExistsException extends RuntimeException {

    public TopicSubscriptionAlreadyExistsException() {
        super("Topic subscription already exists");
    }
}
