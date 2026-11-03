package com.kolyvanova.chatter.security;

import tools.jackson.databind.ObjectMapper;
import com.kolyvanova.chatter.exception.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.List;
import java.util.Map;

final class ApiErrorWriter {

    private ApiErrorWriter() {
    }

    static void write(
            ObjectMapper objectMapper,
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String code,
            String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new ApiErrorResponse(
                status.value(),
                code,
                message,
                List.of(),
                request.getRequestURI(),
                Map.of()
        ));
    }
}
