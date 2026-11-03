package com.kolyvanova.chatter.exception;

import java.util.List;

public record ApiFieldError(
        String code,
        String message,
        List<String> args
) {
}
