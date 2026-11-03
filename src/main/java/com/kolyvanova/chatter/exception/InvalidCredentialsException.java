package com.kolyvanova.chatter.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("error.auth.invalid-credentials");
    }
}
