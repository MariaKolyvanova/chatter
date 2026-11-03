package com.kolyvanova.chatter.exception;

public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException() {
        super("error.file.not-found");
    }
}
