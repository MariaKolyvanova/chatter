package com.kolyvanova.microblogging.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({
            UserNotFoundException.class,
            PostNotFoundException.class,
            LikeNotFoundException.class,
            SubscriptionNotFoundException.class,
            CommentNotFoundException.class,
            RepostNotFoundException.class
    })
    public ResponseEntity<ApiErrorResponse> notFoundHandler(RuntimeException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        ApiErrorResponse response = new ApiErrorResponse(
                status.value(),
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }

    @ExceptionHandler({
            UserAlreadyExistsException.class,
            SubscriptionAlreadyExistsException.class,
            LikeAlreadyExistsException.class,
            RepostAlreadyExistsException.class
    })
    public ResponseEntity<ApiErrorResponse> alreadyExistsHandler(RuntimeException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;

        ApiErrorResponse response = new ApiErrorResponse(
                status.value(),
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }

    @ExceptionHandler(
            SelfSubscriptionException.class
    )
    public ResponseEntity<ApiErrorResponse> selfSubscriptionHandler(
            SelfSubscriptionException exception,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ApiErrorResponse response = new ApiErrorResponse(
                status.value(),
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            String message = fieldError.getDefaultMessage() == null
                    ? "Invalid value"
                    : fieldError.getDefaultMessage();
            fieldErrors.merge(fieldError.getField(), message,
                    (first, next) -> first.equals(next) ? first : first + "; " + next);
        }

        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ApiErrorResponse response = new ApiErrorResponse(
                status.value(),
                "Request validation failed",
                path,
                fieldErrors
        );

        return ResponseEntity.status(status)
                .headers(headers)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> internalServerErrorHandler(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error("Unexpected error while processing request {}", request.getRequestURI(), exception);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiErrorResponse response = new ApiErrorResponse(
                status.value(),
                "An unexpected error occurred",
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity.status(status).body(response);
    }
}