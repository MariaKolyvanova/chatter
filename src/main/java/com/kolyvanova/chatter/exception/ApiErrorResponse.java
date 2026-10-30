package com.kolyvanova.chatter.exception;

import java.util.Map;

public record ApiErrorResponse(
        int status,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
