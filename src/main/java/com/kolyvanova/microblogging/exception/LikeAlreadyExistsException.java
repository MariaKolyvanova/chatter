package com.kolyvanova.microblogging.exception;

public class LikeAlreadyExistsException extends RuntimeException {

    public LikeAlreadyExistsException() {
        super("User has already liked this post");
    }
}
