package com.kolyvanova.microblogging.user;

import com.kolyvanova.microblogging.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserRestController {

    private final UserService userService;
    private final CurrentUser currentUser;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserReadDto create(@Valid @RequestBody UserCreateDto dto) {
        return userService.create(dto);
    }

    @GetMapping
    public Page<UserPublicReadDto> findAll(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return userService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public UserPublicReadDto findById(@PathVariable UUID id) {
        return userService.findById(id);
    }

    @GetMapping("/me")
    public UserReadDto findMe() {
        return userService.findMe(currentUser.id());
    }

    @PutMapping("/me")
    public UserReadDto update(@Valid @RequestBody UserUpdateDto dto) {
        return userService.update(currentUser.id(), dto);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete() {
        userService.delete(currentUser.id());
    }
}