package com.kolyvanova.microblogging.subscription;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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
    public List<SubscriptionReadDto> findAll() {
        return subscriptionService.findAll();
    }

    @GetMapping("/{id}")
    public SubscriptionReadDto findById(@PathVariable UUID id) {
        return subscriptionService.findById(id);
    }

    @GetMapping(params = "subscriberId")
    public List<SubscriptionReadDto> findAllBySubscriberId(@RequestParam UUID subscriberId) {
        return subscriptionService.findAllBySubscriberId(subscriberId);
    }

    @GetMapping(params = "authorId")
    public List<SubscriptionReadDto> findAllByAuthorId(@RequestParam UUID authorId) {
        return subscriptionService.findAllByAuthorId(authorId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        subscriptionService.delete(id);
    }
}
