package com.kolyvanova.chatter.exception;

public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException() {
        super("Current password is incorrect");
    }
}
