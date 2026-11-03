package com.kolyvanova.chatter.exception;

public class LikeNotFoundException extends RuntimeException {

    public LikeNotFoundException() {
        super("error.like.not-found");
    }
}
