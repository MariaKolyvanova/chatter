package com.kolyvanova.chatter.subscription;

import com.kolyvanova.chatter.exception.SelfSubscriptionException;
import com.kolyvanova.chatter.exception.SubscriptionAlreadyExistsException;
import com.kolyvanova.chatter.exception.SubscriptionNotFoundException;
import com.kolyvanova.chatter.exception.UserNotFoundException;
import com.kolyvanova.chatter.user.User;
import com.kolyvanova.chatter.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionMapper subscriptionMapper;

    public SubscriptionReadDto create(UUID subscriberId, UUID authorId) {
        User subscriber = userRepository.findById(subscriberId)
                .orElseThrow(() -> new UserNotFoundException(subscriberId));
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new UserNotFoundException(authorId));
        if (subscriberId.equals(authorId)) {
            throw new SelfSubscriptionException();
        }
        if (subscriptionRepository.existsBySubscriberIdAndAuthorId(subscriberId, authorId)) {
            throw new SubscriptionAlreadyExistsException();
        }

        Subscription savedSubscription = subscriptionRepository.saveAndFlush(Subscription.builder()
                .subscriber(subscriber)
                .author(author)
                .build());
        log.info("Created subscription {} from user {} to author {}", savedSubscription.getId(), subscriberId, authorId);

        return subscriptionMapper.toReadDto(savedSubscription);
    }

    @Transactional(readOnly = true)
    public Page<SubscriptionReadDto> findAll(Pageable pageable) {
        return subscriptionRepository.findAll(pageable).map(subscriptionMapper::toReadDto);
    }

    @Transactional(readOnly = true)
    public SubscriptionReadDto findById(UUID id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new SubscriptionNotFoundException(id));

        return subscriptionMapper.toReadDto(subscription);
    }

    @Transactional(readOnly = true)
    public Page<SubscriptionReadDto> findAllBySubscriberId(UUID subscriberId, Pageable pageable) {
        return subscriptionRepository.findAllBySubscriberId(subscriberId, pageable).map(subscriptionMapper::toReadDto);
    }

    @Transactional(readOnly = true)
    public Page<SubscriptionReadDto> findAllByAuthorId(UUID authorId, Pageable pageable) {
        return subscriptionRepository.findAllByAuthorId(authorId, pageable).map(subscriptionMapper::toReadDto);
    }

    public void delete(UUID subscriberId, UUID authorId) {
        Subscription subscription = subscriptionRepository.findBySubscriberIdAndAuthorId(subscriberId, authorId)
                .orElseThrow(() -> new SubscriptionNotFoundException(subscriberId, authorId));

        subscriptionRepository.delete(subscription);
        log.info("Deleted subscription from user {} to author {}", subscriberId, authorId);
    }
}
