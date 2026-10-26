package com.kolyvanova.microblogging.post;

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

@Tag(name = "posts")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/posts")
public class PostRestController {
    private final PostService postService;
    private final CurrentUser currentUser;

    @Operation(summary = "Create a post")
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
    @GetMapping("/{id}")
    public PostReadDto findById(@PathVariable UUID id) {
        return postService.findById(id, currentUser.id(), currentUser.canModerate());
    }

    @Operation(summary = "List an author's published posts")
    @GetMapping(params = "authorId")
    public Page<PostReadDto> findAllByAuthorId(
            @RequestParam UUID authorId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return postService.findAllByAuthorId(authorId, pageable);
    }

    @Operation(summary = "Update a post")
    @PutMapping("/{id}")
    public PostReadDto update(@PathVariable UUID id, @Valid @RequestBody PostUpdateDto dto) {
        return postService.update(id, currentUser.id(), dto);
    }

    @Operation(summary = "Delete a post")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        postService.delete(id, currentUser.id());
    }

    @Operation(summary = "Hide a post. Moderator or admin only")
    @PutMapping("/{id}/hide")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hide(@PathVariable UUID id) {
        postService.hide(id);
    }

    @Operation(summary = "Show a post. Moderator or admin only")
    @PutMapping("/{id}/show")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void show(@PathVariable UUID id) {
        postService.show(id);
    }
}
