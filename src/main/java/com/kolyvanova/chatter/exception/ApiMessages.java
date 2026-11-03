package com.kolyvanova.chatter.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ApiMessages {

    private final MessageSource messageSource;

    public String code(String code) {
        if (code == null || code.isBlank() || "Access Denied".equals(code)) {
            return "error.access-denied";
        }
        return code;
    }

    public String text(String code, Object... args) {
        String key = code(code);
        Object[] values = args == null || args.length == 0 ? null : args;
        return messageSource.getMessage(key, values, key, Locale.ENGLISH);
    }
}