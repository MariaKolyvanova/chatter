package com.kolyvanova.chatter.exception;

import com.kolyvanova.chatter.post.PostCategory;

import java.util.UUID;

public class TopicSubscriptionNotFoundException extends RuntimeException {

    public TopicSubscriptionNotFoundException(UUID userId, PostCategory category) {
        super("Topic subscription of user " + userId + " to " + category + " not found");
    }
}
