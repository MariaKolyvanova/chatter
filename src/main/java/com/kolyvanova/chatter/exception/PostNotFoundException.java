package com.kolyvanova.chatter.exception;

public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException() {
        super("error.post.not-found");
    }
}
