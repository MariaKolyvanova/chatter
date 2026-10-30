package com.kolyvanova.chatter.topicSubscriptions;

import com.kolyvanova.chatter.exception.TopicSubscriptionAlreadyExistsException;
import com.kolyvanova.chatter.exception.TopicSubscriptionNotFoundException;
import com.kolyvanova.chatter.exception.UserNotFoundException;
import com.kolyvanova.chatter.post.PostCategory;
import com.kolyvanova.chatter.support.AbstractIntegrationTest;
import com.kolyvanova.chatter.user.User;
import com.kolyvanova.chatter.user.UserRepository;
import com.kolyvanova.chatter.user.UserRole;
import com.kolyvanova.chatter.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TopicSubscriptionsServiceTest extends AbstractIntegrationTest {

    @Autowired
    private TopicSubscriptionsService topicSubscriptionsService;

    @Autowired
    private TopicSubscriptionsRepository topicSubscriptionsRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        topicSubscriptionsRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User createUser() {
        String suffix = UUID.randomUUID().toString();
        return userRepository.saveAndFlush(User.builder()
                .username("topic_" + suffix)
                .email(suffix + "@mail.test")
                .passwordHash("test-password-hash")
                .displayName("Topic test user")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .build());
    }

    @Test
    void create() {
        User user = createUser();

        TopicSubscriptionReadDto result = topicSubscriptionsService.create(user.getId(), PostCategory.SCIENCE);

        assertNotNull(result.id());
        assertEquals(user.getId(), result.userId());
        assertEquals(PostCategory.SCIENCE, result.category());
    }

    @Test
    void createDuplicateSubscription() {
        User user = createUser();
        topicSubscriptionsService.create(user.getId(), PostCategory.SCIENCE);

        assertThrows(TopicSubscriptionAlreadyExistsException.class,
                () -> topicSubscriptionsService.create(user.getId(), PostCategory.SCIENCE));
    }

    @Test
    void createFailsWhenUserDoesNotExist() {
        assertThrows(UserNotFoundException.class,
                () -> topicSubscriptionsService.create(UUID.randomUUID(), PostCategory.SCIENCE));
    }

    @Test
    void findMeReturnsOnlyTheCurrentUsersTopics() {
        User user = createUser();
        User other = createUser();
        topicSubscriptionsService.create(user.getId(), PostCategory.SCIENCE);
        topicSubscriptionsService.create(user.getId(), PostCategory.SPORTS);
        topicSubscriptionsService.create(other.getId(), PostCategory.NEWS);

        List<TopicSubscriptionReadDto> result = topicSubscriptionsService.findMe(user.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(item -> user.getId().equals(item.userId())));
        assertTrue(result.stream().map(TopicSubscriptionReadDto::category)
                .toList().containsAll(List.of(PostCategory.SCIENCE, PostCategory.SPORTS)));
    }

    @Test
    void deleteRemovesSubscription() {
        User user = createUser();
        TopicSubscriptionReadDto created = topicSubscriptionsService.create(user.getId(), PostCategory.SCIENCE);

        topicSubscriptionsService.delete(user.getId(), PostCategory.SCIENCE);

        assertFalse(topicSubscriptionsRepository.existsById(created.id()));
    }

    @Test
    void deleteThrowsWhenSubscriptionDoesNotExist() {
        User user = createUser();

        assertThrows(TopicSubscriptionNotFoundException.class,
                () -> topicSubscriptionsService.delete(user.getId(), PostCategory.SCIENCE));
    }
}
