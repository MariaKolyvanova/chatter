package com.kolyvanova.microblogging.like;

import com.kolyvanova.microblogging.exception.ApiErrorResponse;
import com.kolyvanova.microblogging.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "likes")
@ApiResponse(responseCode = "401", description = "Authentication is required",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/likes")
public class LikeRestController {

    private final LikeService likeService;
    private final CurrentUser currentUser;

    @Operation(summary = "Like a post")
    @ApiResponse(responseCode = "404", description = "User or post not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Like already exists",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LikeReadDto create(@RequestParam UUID postId) {
        return likeService.create(postId, currentUser.id());
    }

    @Operation(summary = "List likes")
    @GetMapping
    public Page<LikeReadDto> findAll(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC)
                                         Pageable pageable) {
        return likeService.findAll(pageable);
    }

    @Operation(summary = "Get a like")
    @ApiResponse(responseCode = "404", description = "Like not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @GetMapping("/{id}")
    public LikeReadDto findById(@PathVariable UUID id) {
        return likeService.findById(id, currentUser.id(), currentUser.canModerate());
    }

    @Operation(summary = "List likes of a post")
    @ApiResponse(responseCode = "404", description = "Post not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @GetMapping("/by-post")
    public Page<LikeReadDto> findAllByPostId(@RequestParam UUID postId,
                                             @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC)
                                             Pageable pageable) {
        return likeService.findAllByPostId(postId, currentUser.id(), currentUser.canModerate(), pageable);
    }

    @Operation(summary = "List a user's likes")
    @GetMapping("/by-user")
    public Page<LikeReadDto> findAllByUserId(@RequestParam UUID userId,
                                             @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC)
                                             Pageable pageable) {
        return likeService.findAllByUserId(userId, pageable);
    }

    @Operation(summary = "Unlike a post")
    @ApiResponse(responseCode = "404", description = "Like not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @DeleteMapping(params = "postId")
    public void delete(@RequestParam UUID postId) {
        likeService.delete(currentUser.id(), postId);
    }
}
