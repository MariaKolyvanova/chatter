package com.kolyvanova.chatter.auth;

import com.kolyvanova.chatter.exception.UserAlreadyExistsException;
import com.kolyvanova.chatter.security.JwtService;
import com.kolyvanova.chatter.user.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserReadDto register(UserCreateDto request) {
        return userService.create(request);
    }

    public AuthResponse login(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            SecurityUser user = (SecurityUser) authentication.getPrincipal();
            log.info("User {} logged in", user.getId());

            return new AuthResponse(jwtService.create(user), "Bearer");
        } catch (AuthenticationException exception) {
            log.warn("Login failed for username {}", username);
            throw exception;
        }
    }
}