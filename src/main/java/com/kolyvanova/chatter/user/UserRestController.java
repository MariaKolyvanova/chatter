package com.kolyvanova.chatter.user;

import com.kolyvanova.chatter.exception.ApiErrorResponse;
import com.kolyvanova.chatter.image.ImageResponse;
import com.kolyvanova.chatter.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Tag(name = "users")
@ApiResponse(responseCode = "401", description = "Authentication is required",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserRestController {

    private final UserService userService;
    private final CurrentUser currentUser;

    @Operation(summary = "Create a user")
    @ApiResponse(responseCode = "400", description = "Request validation failed",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Username or email is already taken",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserReadDto create(@Valid @RequestBody UserCreateDto dto) {
        return userService.create(dto);
    }

    @Operation(summary = "List users. An admin also receives each user's role")
    @ApiResponse(responseCode = "403", description = "Access is denied",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @GetMapping
    public Object findAll(
            @RequestParam(required = false) UserRole role,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        if (role != null) {
            userService.requireAdmin(currentUser.id());
            return userService.findAllForAdmin(role, pageable);
        }
        if (userService.isAdmin(currentUser.id())) {
            return userService.findAllForAdmin(null, pageable);
        }
        return userService.findAll(pageable);
    }

    @Operation(summary = "Get a user. An admin also receives the role")
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @GetMapping("/{id}")
    public Object findById(@PathVariable UUID id) {
        if (userService.isAdmin(currentUser.id())) {
            return userService.findForAdmin(id);
        }
        return userService.findById(id);
    }

    @SecurityRequirements
    @Operation(summary = "Get a user's avatar")
    @ApiResponse(responseCode = "404", description = "User or avatar not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @GetMapping("/{id}/avatar")
    public ResponseEntity<Resource> avatar(@PathVariable UUID id) {
        ImageResponse image = userService.loadAvatar(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .body(image.resource());
    }

    @Operation(summary = "Get a user by username")
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @GetMapping("/by-username")
    public Object findByUsername(@RequestParam String username) {
        if (userService.isAdmin(currentUser.id())) {
            return userService.findByUsernameForAdmin(username);
        }
        return userService.findByUsername(username);
    }

    @Operation(summary = "Get the current user")
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @GetMapping("/me")
    public UserReadDto findMe() {
        return userService.findMe(currentUser.id());
    }

    @Operation(summary = "Update the current user")
    @ApiResponse(responseCode = "400", description = "Request validation failed",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Username or email is already taken",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PutMapping("/me")
    public UserReadDto update(@Valid @RequestBody UserUpdateDto dto) {
        return userService.update(currentUser.id(), dto);
    }

    @Operation(summary = "Change the current user's password")
    @ApiResponse(responseCode = "400", description = "Validation failed or current password is incorrect",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordDto dto) {
        userService.changePassword(currentUser.id(), dto.currentPassword(), dto.newPassword());
    }

    @Operation(summary = "Update the current user's avatar")
    @ApiResponse(responseCode = "400", description = "Image is empty or has an unsupported type",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PutMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserReadDto updateAvatar(@RequestParam MultipartFile file) throws IOException {
        return userService.updateAvatar(currentUser.id(), file);
    }

    @Operation(summary = "Change a user's role. Admin only")
    @ApiResponse(responseCode = "400", description = "Invalid role",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Access is denied",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PutMapping("/{id}/role")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeRole(@PathVariable UUID id, @RequestParam UserRole role) {
        userService.changeRole(id, currentUser.id(), role);
    }

    @Operation(summary = "Block a user. Moderator or admin only")
    @ApiResponse(responseCode = "403", description = "Access is denied",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PutMapping("/{id}/block")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void block(@PathVariable UUID id) {
        userService.block(id);
    }

    @Operation(summary = "Unblock a user. Moderator or admin only")
    @ApiResponse(responseCode = "403", description = "Access is denied",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PutMapping("/{id}/unblock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unblock(@PathVariable UUID id) {
        userService.unblock(id);
    }

    @Operation(summary = "Delete the current user")
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete() {
        userService.delete(currentUser.id());
    }
}