package com.kolyvanova.microblogging.exception;

public class TopicSubscriptionAlreadyExistsException extends RuntimeException {

    public TopicSubscriptionAlreadyExistsException() {
        super("Topic subscription already exists");
    }
}
