package com.kolyvanova.chatter.exception;

import java.util.List;
import java.util.Map;

public record ApiErrorResponse(
        int status,
        String code,
        String message,
        List<String> args,
        String path,
        Map<String, ApiFieldError> fieldErrors
) {
}
