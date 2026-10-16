package com.kolyvanova.microblogging.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class LikeNotFoundException extends RuntimeException {

    public LikeNotFoundException(UUID id) {
        super("Like not found: " + id);
    }
}
