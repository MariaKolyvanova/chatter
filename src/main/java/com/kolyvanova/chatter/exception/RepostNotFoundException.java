package com.kolyvanova.chatter.exception;

public class RepostNotFoundException extends RuntimeException {

    public RepostNotFoundException() {
        super("error.repost.not-found");
    }
}
