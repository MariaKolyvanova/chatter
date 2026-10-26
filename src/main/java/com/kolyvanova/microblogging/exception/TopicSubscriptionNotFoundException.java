package com.kolyvanova.microblogging.exception;

import com.kolyvanova.microblogging.post.PostCategory;

import java.util.UUID;

public class TopicSubscriptionNotFoundException extends RuntimeException {

    public TopicSubscriptionNotFoundException(UUID userId, PostCategory category) {
        super("Topic subscription of user " + userId + " to " + category + " not found");
    }
}
