package com.kolyvanova.microblogging.user;

import com.kolyvanova.microblogging.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "users")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserRestController {

    private final UserService userService;
    private final CurrentUser currentUser;

    @Operation(summary = "Create a user")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserReadDto create(@Valid @RequestBody UserCreateDto dto) {
        return userService.create(dto);
    }

    @Operation(summary = "List users")
    @GetMapping
    public Page<UserPublicReadDto> findAll(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return userService.findAll(pageable);
    }

    @Operation(summary = "Get a user")
    @GetMapping("/{id}")
    public UserPublicReadDto findById(@PathVariable UUID id) {
        return userService.findById(id);
    }

    @Operation(summary = "Get a user by username")
    @GetMapping(params = "username")
    public UserPublicReadDto findByUsername(@RequestParam String username) {
        return userService.findByUsername(username);
    }

    @Operation(summary = "Get the current user")
    @GetMapping("/me")
    public UserReadDto findMe() {
        return userService.findMe(currentUser.id());
    }

    @Operation(summary = "Update the current user")
    @PutMapping("/me")
    public UserReadDto update(@Valid @RequestBody UserUpdateDto dto) {
        return userService.update(currentUser.id(), dto);
    }

    @Operation(summary = "Delete the current user")
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete() {
        userService.delete(currentUser.id());
    }
}