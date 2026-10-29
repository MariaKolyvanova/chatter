package com.kolyvanova.microblogging.post;

import com.kolyvanova.microblogging.exception.ApiErrorResponse;
import com.kolyvanova.microblogging.image.ImageResponse;
import com.kolyvanova.microblogging.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

@Tag(name = "posts")
@ApiResponse(responseCode = "401", description = "Authentication is required",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/posts")
public class PostRestController {
    private final PostService postService;
    private final CurrentUser currentUser;

    @Operation(summary = "Create a post")
    @ApiResponse(responseCode = "400", description = "Request validation failed",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "User or parent post not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostReadDto create(@Valid @RequestBody PostCreateDto dto) {
        return postService.create(dto, currentUser.id());
    }

    @Operation(summary = "Get the feed of followed authors")
    @GetMapping("/feed")
    public Page<PostReadDto> findFeed(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return postService.findFeed(currentUser.id(), pageable);
    }

    @Operation(summary = "List published posts")
    @GetMapping()
    public Page<PostReadDto> findAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return postService.findAll(pageable);
    }

    @Operation(summary = "List the current user's posts")
    @GetMapping("/me")
    public Page<PostReadDto> findMine(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return postService.findMine(currentUser.id(), pageable);
    }

    @Operation(summary = "Get a post")
    @ApiResponse(responseCode = "404", description = "Post not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @GetMapping("/{id}")
    public PostReadDto findById(@PathVariable UUID id) {
        return postService.findById(id, currentUser.id(), currentUser.canModerate());
    }

    @Operation(summary = "Get a post image")
    @ApiResponse(responseCode = "404", description = "Post or image not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> image(@PathVariable UUID id) {
        ImageResponse image = postService.loadImage(id, currentUser.id(), currentUser.canModerate());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .body(image.resource());
    }

    @Operation(summary = "List an author's published posts")
    @GetMapping(value = "by-author", params = "authorId")
    public Page<PostReadDto> findAllByAuthorId(
            @RequestParam UUID authorId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return postService.findAllByAuthorId(authorId, pageable);
    }

    @Operation(summary = "Get the feed of followed topics")
    @GetMapping("/interests")
    public Page<PostReadDto> findAllByInterests(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return postService.findAllByInterests(currentUser.id(), pageable);
    }

    @Operation(summary = "Update a post")
    @ApiResponse(responseCode = "400", description = "Request validation failed",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Access is denied",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Post not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PutMapping("/{id}")
    public PostReadDto update(@PathVariable UUID id, @Valid @RequestBody PostUpdateDto dto) {
        return postService.update(id, currentUser.id(), dto);
    }

    @Operation(summary = "Add an image to a post")
    @ApiResponse(responseCode = "400", description = "Image is empty or has an unsupported type",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Access is denied",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Post not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PostReadDto updateImage(@PathVariable UUID id, @RequestParam MultipartFile file) throws IOException {
        return postService.updateImage(id, currentUser.id(), file);
    }

    @Operation(summary = "Delete a post")
    @ApiResponse(responseCode = "403", description = "Access is denied",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Post not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        postService.delete(id, currentUser.id());
    }

    @Operation(summary = "Hide a post. Moderator or admin only")
    @ApiResponse(responseCode = "403", description = "Access is denied",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Post not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PutMapping("/{id}/hide")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hide(@PathVariable UUID id) {
        postService.hide(id);
    }

    @Operation(summary = "Show a post. Moderator or admin only")
    @ApiResponse(responseCode = "403", description = "Access is denied",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Post not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PutMapping("/{id}/show")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void show(@PathVariable UUID id) {
        postService.show(id);
    }
}
