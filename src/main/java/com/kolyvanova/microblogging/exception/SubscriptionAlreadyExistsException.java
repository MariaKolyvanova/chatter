package com.kolyvanova.microblogging.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class SubscriptionAlreadyExistsException extends RuntimeException {

    public SubscriptionAlreadyExistsException() {
        super("Subscription already exists");
    }
}
