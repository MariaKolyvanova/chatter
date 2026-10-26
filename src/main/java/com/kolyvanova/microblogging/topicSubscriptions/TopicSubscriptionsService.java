package com.kolyvanova.microblogging.topicSubscriptions;

import com.kolyvanova.microblogging.exception.TopicSubscriptionAlreadyExistsException;
import com.kolyvanova.microblogging.exception.TopicSubscriptionNotFoundException;
import com.kolyvanova.microblogging.exception.UserNotFoundException;
import com.kolyvanova.microblogging.post.PostCategory;
import com.kolyvanova.microblogging.user.User;
import com.kolyvanova.microblogging.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class TopicSubscriptionsService {
    private final TopicSubscriptionsRepository topicSubscriptionsRepository;
    private final UserRepository userRepository;

    public TopicSubscriptionReadDto create(UUID userId, PostCategory category) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        if (topicSubscriptionsRepository.existsByUserIdAndCategory(userId, category)) {
            throw new TopicSubscriptionAlreadyExistsException();
        }

        TopicSubscriptions saved = topicSubscriptionsRepository.save(TopicSubscriptions.builder()
                .user(user)
                .category(category)
                .build());
        log.info("Created topic subscription {} for user {} on {}", saved.getId(), userId, category);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<TopicSubscriptionReadDto> findMe(UUID userId) {
        return topicSubscriptionsRepository.findAllByUserId(userId).stream()
                .map(this::toDto)
                .toList();
    }

    public void delete(UUID userId, PostCategory category) {
        TopicSubscriptions topicSubscriptions = topicSubscriptionsRepository.findByUserIdAndCategory(userId, category)
                .orElseThrow(() -> new TopicSubscriptionNotFoundException(userId, category));
        topicSubscriptionsRepository.delete(topicSubscriptions);
        log.info("Deleted topic subscription of user {} on {}", userId, category);
    }

    private TopicSubscriptionReadDto toDto(TopicSubscriptions subscription) {
        return new TopicSubscriptionReadDto(
                subscription.getId(),
                subscription.getUser().getId(),
                subscription.getCategory()
        );
    }
}
