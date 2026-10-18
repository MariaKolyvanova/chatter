package com.kolyvanova.microblogging.subscription;

import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionRestController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionReadDto create(@Valid @RequestBody SubscriptionCreateDto dto) {
        return subscriptionService.create(dto);
    }

    @GetMapping
    public Page<SubscriptionReadDto> findAll(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return subscriptionService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public SubscriptionReadDto findById(@PathVariable UUID id) {
        return subscriptionService.findById(id);
    }

    @GetMapping(params = "subscriberId")
    public Page<SubscriptionReadDto> findAllBySubscriberId(@RequestParam UUID subscriberId,
                                                          @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return subscriptionService.findAllBySubscriberId(subscriberId, pageable);
    }

    @GetMapping(params = "authorId")
    public Page<SubscriptionReadDto> findAllByAuthorId(@RequestParam UUID authorId,
                                                       @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return subscriptionService.findAllByAuthorId(authorId, pageable);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        subscriptionService.delete(id);
    }
}
