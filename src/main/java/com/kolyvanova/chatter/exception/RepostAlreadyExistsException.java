package com.kolyvanova.chatter.exception;

public class RepostAlreadyExistsException extends RuntimeException {

    public RepostAlreadyExistsException() {
        super("User has already reposted this post");
    }
}
