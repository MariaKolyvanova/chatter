package com.kolyvanova.chatter.exception;

public class LikeAlreadyExistsException extends RuntimeException {

    public LikeAlreadyExistsException() {
        super("error.like.already-exists");
    }
}
