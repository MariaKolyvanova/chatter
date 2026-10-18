package com.kolyvanova.microblogging.exception;

import java.util.UUID;

public class LikeNotFoundException extends RuntimeException {

    public LikeNotFoundException(UUID id) {
        super("Like not found: " + id);
    }
}
