package com.kolyvanova.microblogging.subscription;

import com.kolyvanova.microblogging.exception.SelfSubscriptionException;
import com.kolyvanova.microblogging.exception.SubscriptionAlreadyExistsException;
import com.kolyvanova.microblogging.exception.SubscriptionNotFoundException;
import com.kolyvanova.microblogging.exception.UserNotFoundException;
import com.kolyvanova.microblogging.user.User;
import com.kolyvanova.microblogging.user.UserRepository;
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

    public SubscriptionReadDto create(SubscriptionCreateDto dto, UUID subscriberId) {
        User subscriber = userRepository.findById(subscriberId)
                .orElseThrow(() -> new UserNotFoundException(subscriberId));
        User author = userRepository.findById(dto.authorId())
                .orElseThrow(() -> new UserNotFoundException(dto.authorId()));
        if (subscriberId.equals(dto.authorId())) {
            throw new SelfSubscriptionException();
        }
        if (subscriptionRepository.existsBySubscriberIdAndAuthorId(subscriberId, dto.authorId())) {
            throw new SubscriptionAlreadyExistsException();
        }

        Subscription subscription = subscriptionMapper.toEntity(dto);
        subscription.setSubscriber(subscriber);
        subscription.setAuthor(author);
        Subscription savedSubscription = subscriptionRepository.saveAndFlush(subscription);
        log.info("Created subscription {} from user {} to author {}", savedSubscription.getId(), subscriberId, dto.authorId());

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
