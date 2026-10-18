package com.kolyvanova.microblogging.exception;

public class RepostAlreadyExistsException extends RuntimeException {

    public RepostAlreadyExistsException() {
        super("User has already reposted this post");
    }
}
