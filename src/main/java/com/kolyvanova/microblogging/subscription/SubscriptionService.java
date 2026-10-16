package com.kolyvanova.microblogging.subscription;

import com.kolyvanova.microblogging.exception.SelfSubscriptionException;
import com.kolyvanova.microblogging.exception.SubscriptionAlreadyExistsException;
import com.kolyvanova.microblogging.exception.SubscriptionNotFoundException;
import com.kolyvanova.microblogging.exception.UserNotFoundException;
import com.kolyvanova.microblogging.user.User;
import com.kolyvanova.microblogging.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionMapper subscriptionMapper;

    @Transactional
    public SubscriptionReadDto create(SubscriptionCreateDto dto) {
        if (dto.subscriberId().equals(dto.authorId())) {
            throw new SelfSubscriptionException();
        }

        User subscriber = userRepository.findById(dto.subscriberId())
                .orElseThrow(() -> new UserNotFoundException(dto.subscriberId()));
        User author = userRepository.findById(dto.authorId())
                .orElseThrow(() -> new UserNotFoundException(dto.authorId()));

        if (subscriptionRepository.existsBySubscriberIdAndAuthorId(dto.subscriberId(), dto.authorId())) {
            throw new SubscriptionAlreadyExistsException();
        }

        Subscription subscription = subscriptionMapper.toEntity(dto);
        subscription.setSubscriber(subscriber);
        subscription.setAuthor(author);

        return subscriptionMapper.toReadDto(subscriptionRepository.save(subscription));
    }

    @Transactional(readOnly = true)
    public List<SubscriptionReadDto> findAll() {
        return subscriptionRepository.findAll().stream()
                .map(subscriptionMapper::toReadDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubscriptionReadDto findById(UUID id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new SubscriptionNotFoundException(id));

        return subscriptionMapper.toReadDto(subscription);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionReadDto> findAllBySubscriberId(UUID subscriberId) {
        return subscriptionRepository.findAllBySubscriberId(subscriberId).stream()
                .map(subscriptionMapper::toReadDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubscriptionReadDto> findAllByAuthorId(UUID authorId) {
        return subscriptionRepository.findAllByAuthorId(authorId).stream()
                .map(subscriptionMapper::toReadDto)
                .toList();
    }

    @Transactional
    public void delete(UUID id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new SubscriptionNotFoundException(id));

        subscriptionRepository.delete(subscription);
    }
}
