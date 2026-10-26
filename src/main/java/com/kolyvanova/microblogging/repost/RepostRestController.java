package com.kolyvanova.microblogging.repost;

import com.kolyvanova.microblogging.exception.ApiErrorResponse;
import com.kolyvanova.microblogging.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "reposts")
@ApiResponse(responseCode = "401", description = "Authentication is required",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reposts")
public class RepostRestController {

    private final RepostService repostService;
    private final CurrentUser currentUser;

    @Operation(summary = "Repost a post")
    @ApiResponse(responseCode = "404", description = "User or post not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Repost already exists",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RepostReadDto create(@RequestParam UUID postId) {
        return repostService.create(postId, currentUser.id());
    }

    @Operation(summary = "List reposts")
    @GetMapping
    public Page<RepostReadDto> findAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return repostService.findAll(pageable);
    }

    @Operation(summary = "Get a repost")
    @ApiResponse(responseCode = "404", description = "Repost not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @GetMapping("/{id}")
    public RepostReadDto findById(@PathVariable UUID id) {
        return repostService.findById(id, currentUser.id(), currentUser.canModerate());
    }

    @Operation(summary = "List reposts of a post")
    @ApiResponse(responseCode = "404", description = "Post not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @GetMapping(params = "postId")
    public Page<RepostReadDto> findAllByPostId(
            @RequestParam UUID postId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return repostService.findAllByPostId(postId, currentUser.id(), currentUser.canModerate(), pageable);
    }

    @Operation(summary = "List a user's reposts")
    @GetMapping(params = "userId")
    public Page<RepostReadDto> findAllByUserId(
            @RequestParam UUID userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return repostService.findAllByUserId(userId, pageable);
    }

    @Operation(summary = "Delete a repost")
    @ApiResponse(responseCode = "404", description = "Repost not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        repostService.delete(id, currentUser.id());
    }
}
