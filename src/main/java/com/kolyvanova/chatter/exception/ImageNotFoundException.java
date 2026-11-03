package com.kolyvanova.chatter.exception;

public class ImageNotFoundException extends RuntimeException {

    public ImageNotFoundException() {
        super("error.image.not-found");
    }
}
