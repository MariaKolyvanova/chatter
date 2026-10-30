package com.kolyvanova.chatter.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
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

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({
            UserNotFoundException.class,
            PostNotFoundException.class,
            LikeNotFoundException.class,
            SubscriptionNotFoundException.class,
            RepostNotFoundException.class,
            TopicSubscriptionNotFoundException.class,
            ImageNotFoundException.class,
            FileNotFoundException.class
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
            RepostAlreadyExistsException.class,
            TopicSubscriptionAlreadyExistsException.class
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

    @ExceptionHandler({
            SelfSubscriptionException.class,
            InvalidPasswordException.class,
            InvalidFileException.class
    })
    public ResponseEntity<ApiErrorResponse> badRequestHandler(
            RuntimeException exception,
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

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> accessDeniedHandler(HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                status.value(),
                "Access is denied",
                request.getRequestURI(),
                Map.of()
        ));
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