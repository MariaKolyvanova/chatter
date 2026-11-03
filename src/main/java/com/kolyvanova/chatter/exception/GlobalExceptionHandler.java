package com.kolyvanova.chatter.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final ApiMessages messages;

    @ExceptionHandler({
            UserNotFoundException.class,
            PostNotFoundException.class,
            LikeNotFoundException.class,
            SubscriptionNotFoundException.class,
            RepostNotFoundException.class,
            ImageNotFoundException.class,
            FileNotFoundException.class
    })
    public ResponseEntity<ApiErrorResponse> notFoundHandler(RuntimeException exception, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler({
            UserAlreadyExistsException.class,
            SubscriptionAlreadyExistsException.class,
            LikeAlreadyExistsException.class,
            RepostAlreadyExistsException.class
    })
    public ResponseEntity<ApiErrorResponse> alreadyExistsHandler(RuntimeException exception, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler({
            SelfSubscriptionException.class,
            InvalidPasswordException.class,
            InvalidFileException.class
    })
    public ResponseEntity<ApiErrorResponse> badRequestHandler(RuntimeException exception, HttpServletRequest request) {
        Object[] args = exception instanceof InvalidFileException file && file.args() != null
                ? file.args()
                : new Object[0];
        return response(HttpStatus.BAD_REQUEST, exception.getMessage(), request, args);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Map<String, ApiFieldError> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError(fieldError));
        }

        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ApiErrorResponse body = new ApiErrorResponse(
                status.value(),
                "error.validation",
                messages.text("error.validation"),
                List.of(),
                path,
                fieldErrors
        );
        return ResponseEntity.status(status).headers(headers).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> accessDeniedHandler(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.FORBIDDEN, exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> invalidCredentialsHandler(
            InvalidCredentialsException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.UNAUTHORIZED, exception.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> internalServerErrorHandler(Exception exception, HttpServletRequest request) {
        log.error("Unexpected error while processing request {}", request.getRequestURI(), exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "error.unexpected", request);
    }

    private ResponseEntity<ApiErrorResponse> response(
            HttpStatus status,
            String rawCode,
            HttpServletRequest request,
            Object... args
    ) {
        String code = messages.code(rawCode);
        List<String> values = new ArrayList<>();
        if (args != null) {
            for (Object arg : args) {
                if (arg != null) {
                    values.add(String.valueOf(arg));
                }
            }
        }
        ApiErrorResponse body = new ApiErrorResponse(
                status.value(),
                code,
                messages.text(code, args),
                values,
                request.getRequestURI(),
                Map.of()
        );
        return ResponseEntity.status(status).body(body);
    }

    private ApiFieldError fieldError(FieldError fieldError) {
        String code = "error.invalid-value";
        List<String> args = new ArrayList<>();
        try {
            ConstraintViolation<?> violation = fieldError.unwrap(ConstraintViolation.class);
            String template = violation.getMessageTemplate();
            if (template != null && template.startsWith("{") && template.endsWith("}") && template.length() > 2) {
                code = template.substring(1, template.length() - 1);
            }
            Number min = number(violation, "min");
            Number max = number(violation, "max");
            if (min != null && min.longValue() > 0) {
                args.add(min.toString());
            }
            if (max != null && max.longValue() < Integer.MAX_VALUE) {
                args.add(max.toString());
            }
        } catch (IllegalArgumentException ignored) {
            code = "error.invalid-value";
        }
        return new ApiFieldError(code, messages.text(code, args.toArray()), args);
    }

    private Number number(ConstraintViolation<?> violation, String name) {
        Object value = violation.getConstraintDescriptor().getAttributes().get(name);
        return value instanceof Number number ? number : null;
    }
}
