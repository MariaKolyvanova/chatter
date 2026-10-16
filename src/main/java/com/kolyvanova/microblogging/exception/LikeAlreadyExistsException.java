package com.kolyvanova.microblogging.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class LikeAlreadyExistsException extends RuntimeException {

    public LikeAlreadyExistsException() {
        super("User has already liked this post");
    }
}
