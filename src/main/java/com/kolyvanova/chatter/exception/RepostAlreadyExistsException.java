package com.kolyvanova.chatter.exception;

public class RepostAlreadyExistsException extends RuntimeException {

    public RepostAlreadyExistsException() {
        super("error.repost.already-exists");
    }
}
