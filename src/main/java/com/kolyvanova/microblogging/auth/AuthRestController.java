package com.kolyvanova.microblogging.auth;

import com.kolyvanova.microblogging.user.UserCreateDto;
import com.kolyvanova.microblogging.user.UserReadDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "auth")
@SecurityRequirements
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthRestController {
    private final AuthService authService;

    @Operation(summary = "Register a user")
    @PostMapping("/register")
    public ResponseEntity<UserReadDto> register(@Valid @RequestBody UserCreateDto userCreateDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(userCreateDto));
    }


    @Operation(summary = "Log in")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            return authService.login(request.username(), request.password());
        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials");
        }
    }
}
