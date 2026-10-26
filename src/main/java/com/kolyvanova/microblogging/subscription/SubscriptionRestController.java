package com.kolyvanova.microblogging.subscription;

import com.kolyvanova.microblogging.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "subscriptions")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionRestController {

    private final SubscriptionService subscriptionService;
    private final CurrentUser currentUser;

    @Operation(summary = "Subscribe to an author")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionReadDto create(@RequestParam UUID authorId) {
        return subscriptionService.create(currentUser.id(), authorId);
    }

    @Operation(summary = "List subscriptions")
    @GetMapping
    public Page<SubscriptionReadDto> findAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return subscriptionService.findAll(pageable);
    }

    @Operation(summary = "Get a subscription")
    @GetMapping("/{id}")
    public SubscriptionReadDto findById(@PathVariable UUID id) {
        return subscriptionService.findById(id);
    }

    @Operation(summary = "List a user's subscriptions")
    @GetMapping(params = "subscriberId")
    public Page<SubscriptionReadDto> findAllBySubscriberId(
            @RequestParam UUID subscriberId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return subscriptionService.findAllBySubscriberId(subscriberId, pageable);
    }

    @Operation(summary = "List an author's subscribers")
    @GetMapping(params = "authorId")
    public Page<SubscriptionReadDto> findAllByAuthorId(
            @RequestParam UUID authorId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return subscriptionService.findAllByAuthorId(authorId, pageable);
    }

    @Operation(summary = "Unsubscribe from an author")
    @DeleteMapping(params = "authorId")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestParam UUID authorId) {
        subscriptionService.delete(currentUser.id(), authorId);
    }
}
