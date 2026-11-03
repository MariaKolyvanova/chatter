package com.kolyvanova.chatter.exception;

public class InvalidFileException extends RuntimeException {

    private final Object[] args;

    public InvalidFileException(String code, Object... args) {
        super(code);
        this.args = args;
    }

    public Object[] args() {
        return args;
    }
}
