package com.kolyvanova.chatter.exception;

public class ImageNotFoundException extends RuntimeException {
    public ImageNotFoundException(String key) {
        super("Image not found: " + key);
    }
}
