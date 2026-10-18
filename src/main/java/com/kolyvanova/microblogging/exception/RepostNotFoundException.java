package com.kolyvanova.microblogging.exception;

import java.util.UUID;

public class RepostNotFoundException extends RuntimeException {

    public RepostNotFoundException(UUID id) {
        super("Repost not found: " + id);
    }
}
