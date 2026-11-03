package com.kolyvanova.chatter.exception;

public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException() {
        super("error.password.current");
    }
}
