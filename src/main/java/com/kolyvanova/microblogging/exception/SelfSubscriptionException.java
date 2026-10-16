package com.kolyvanova.microblogging.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SelfSubscriptionException extends RuntimeException {

    public SelfSubscriptionException() {
        super("A user cannot subscribe to themselves");
    }
}
