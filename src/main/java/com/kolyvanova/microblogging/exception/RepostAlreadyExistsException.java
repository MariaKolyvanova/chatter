package com.kolyvanova.microblogging.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RepostAlreadyExistsException extends RuntimeException {

    public RepostAlreadyExistsException() {
        super("User has already reposted this post");
    }
}
