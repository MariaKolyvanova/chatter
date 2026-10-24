package com.kolyvanova.microblogging.exception;

import java.util.UUID;

public class LikeNotFoundException extends RuntimeException {

    public LikeNotFoundException(UUID userId, UUID postId) {
        super("Like by user " + userId + " on post " + postId + " not found");
    }

    public LikeNotFoundException(UUID id) {
        super("Like not found: " + id);
    }
}
