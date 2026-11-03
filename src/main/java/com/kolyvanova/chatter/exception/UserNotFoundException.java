package com.kolyvanova.chatter.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("error.user.not-found");
    }
}
