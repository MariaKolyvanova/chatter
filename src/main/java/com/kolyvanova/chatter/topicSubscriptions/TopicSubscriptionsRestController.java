package com.kolyvanova.chatter.topicSubscriptions;

import com.kolyvanova.chatter.exception.ApiErrorResponse;
import com.kolyvanova.chatter.post.PostCategory;
import com.kolyvanova.chatter.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "topic subscriptions")
@ApiResponse(responseCode = "401", description = "Authentication is required",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/topicsSub")
public class TopicSubscriptionsRestController {

    private final TopicSubscriptionsService topicSubscriptionsService;
    private final CurrentUser currentUser;

    @Operation(summary = "Subscribe to a topic")
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Topic subscription already exists",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TopicSubscriptionReadDto create(@RequestParam PostCategory postCategory) {
        return topicSubscriptionsService.create(currentUser.id(), postCategory);
    }

    @Operation(summary = "List the current user's topics")
    @GetMapping("/me")
    public List<TopicSubscriptionReadDto> findMe() {
        return topicSubscriptionsService.findMe(currentUser.id());
    }

    @Operation(summary = "Unsubscribe from a topic")
    @ApiResponse(responseCode = "404", description = "Topic subscription not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestParam PostCategory postCategory) {
        topicSubscriptionsService.delete(currentUser.id(), postCategory);
    }
}
