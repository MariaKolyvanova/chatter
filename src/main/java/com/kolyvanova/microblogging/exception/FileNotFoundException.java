package com.kolyvanova.microblogging.exception;

public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException(String key) {
        super("File not found: " + key);
    }

    public FileNotFoundException() {
        super("File not found");
    }
}
