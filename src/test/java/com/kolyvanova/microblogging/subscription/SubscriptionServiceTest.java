package com.kolyvanova.microblogging.subscription;

import com.kolyvanova.microblogging.exception.SelfSubscriptionException;
import com.kolyvanova.microblogging.exception.SubscriptionAlreadyExistsException;
import com.kolyvanova.microblogging.exception.SubscriptionNotFoundException;
import com.kolyvanova.microblogging.exception.UserNotFoundException;
import com.kolyvanova.microblogging.support.AbstractIntegrationTest;
import com.kolyvanova.microblogging.user.User;
import com.kolyvanova.microblogging.user.UserRepository;
import com.kolyvanova.microblogging.user.UserRole;
import com.kolyvanova.microblogging.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class SubscriptionServiceTest extends AbstractIntegrationTest {

    @Autowired private SubscriptionService subscriptionService;
    @Autowired private SubscriptionRepository subscriptionRepository;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User createUser() {
        String suffix = UUID.randomUUID().toString();
        return userRepository.saveAndFlush(User.builder()
                .username("sub_" + suffix).email(suffix + "@mail.test")
                .passwordHash("test-password-hash").displayName("Subscription test user")
                .role(UserRole.USER).status(UserStatus.ACTIVE).createdAt(Instant.now()).build());
    }

    private SubscriptionCreateDto createDto(User subscriber, User author) {
        return new SubscriptionCreateDto(subscriber.getId(), author.getId());
    }

    @Test
    void create() {
        User subscriber = createUser();
        User author = createUser();
        SubscriptionReadDto result = subscriptionService.create(createDto(subscriber, author));
        assertNotNull(result.id());
        assertEquals(subscriber.getId(), result.subscriberId());
        assertEquals(author.getId(), result.authorId());
        assertNotNull(result.createdAt());
    }

    @Test
    void createDuplicateSubscription() {
        User subscriber = createUser();
        User author = createUser();
        SubscriptionCreateDto dto = createDto(subscriber, author);
        subscriptionService.create(dto);
        assertThrows(SubscriptionAlreadyExistsException.class, () -> subscriptionService.create(dto));
    }

    @Test
    void createFailsWhenUserSubscribesToSelf() {
        User user = createUser();
        assertThrows(SelfSubscriptionException.class,
                () -> subscriptionService.create(new SubscriptionCreateDto(user.getId(), user.getId())));
    }

    @Test
    void createFailsWhenSubscriberDoesNotExist() {
        User author = createUser();
        assertThrows(UserNotFoundException.class,
                () -> subscriptionService.create(new SubscriptionCreateDto(UUID.randomUUID(), author.getId())));
    }

    @Test
    void createFailsWhenAuthorDoesNotExist() {
        User subscriber = createUser();
        assertThrows(UserNotFoundException.class,
                () -> subscriptionService.create(new SubscriptionCreateDto(subscriber.getId(), UUID.randomUUID())));
    }

    @Test
    void findById() {
        User subscriber = createUser();
        User author = createUser();
        SubscriptionReadDto created = subscriptionService.create(createDto(subscriber, author));
        SubscriptionReadDto result = subscriptionService.findById(created.id());
        assertEquals(created.id(), result.id());
        assertEquals(subscriber.getId(), result.subscriberId());
        assertEquals(author.getId(), result.authorId());
    }

    @Test
    void findByIdThrowsWhenSubscriptionDoesNotExist() {
        assertThrows(SubscriptionNotFoundException.class,
                () -> subscriptionService.findById(UUID.randomUUID()));
    }

    @Test
    void findAll() {
        SubscriptionReadDto first = subscriptionService.create(createDto(createUser(), createUser()));
        SubscriptionReadDto second = subscriptionService.create(createDto(createUser(), createUser()));
        Page<SubscriptionReadDto> firstPage = subscriptionService.findAll(PageRequest.of(0, 1));
        Page<SubscriptionReadDto> secondPage = subscriptionService.findAll(PageRequest.of(1, 1));
        assertEquals(1, firstPage.getNumberOfElements());
        assertEquals(2, firstPage.getTotalElements());
        assertEquals(2, firstPage.getTotalPages());
        assertTrue(firstPage.hasNext());
        assertTrue(secondPage.isLast());
        assertTrue(List.of(first.id(), second.id()).contains(firstPage.getContent().getFirst().id()));
        assertTrue(List.of(first.id(), second.id()).contains(secondPage.getContent().getFirst().id()));
    }

    @Test
    void findAllBySubscriberId() {
        User subscriber = createUser();
        SubscriptionReadDto first = subscriptionService.create(createDto(subscriber, createUser()));
        SubscriptionReadDto second = subscriptionService.create(createDto(subscriber, createUser()));
        subscriptionService.create(createDto(createUser(), createUser()));
        Page<SubscriptionReadDto> result = subscriptionService.findAllBySubscriberId(subscriber.getId(), PageRequest.of(0, 1));
        Page<SubscriptionReadDto> secondPage = subscriptionService.findAllBySubscriberId(subscriber.getId(), PageRequest.of(1, 1));
        assertEquals(1, result.getNumberOfElements());
        assertEquals(2, result.getTotalElements());
        assertTrue(result.hasNext());
        assertTrue(secondPage.isLast());
        assertTrue(List.of(first.id(), second.id()).contains(result.getContent().getFirst().id()));
        assertTrue(List.of(first.id(), second.id()).contains(secondPage.getContent().getFirst().id()));
        assertTrue(result.getContent().stream().allMatch(item -> subscriber.getId().equals(item.subscriberId())));
    }

    @Test
    void findAllByAuthorId() {
        User author = createUser();
        SubscriptionReadDto first = subscriptionService.create(createDto(createUser(), author));
        SubscriptionReadDto second = subscriptionService.create(createDto(createUser(), author));
        subscriptionService.create(createDto(createUser(), createUser()));
        Page<SubscriptionReadDto> result = subscriptionService.findAllByAuthorId(author.getId(), PageRequest.of(0, 1));
        Page<SubscriptionReadDto> secondPage = subscriptionService.findAllByAuthorId(author.getId(), PageRequest.of(1, 1));
        assertEquals(1, result.getNumberOfElements());
        assertEquals(2, result.getTotalElements());
        assertTrue(result.hasNext());
        assertTrue(secondPage.isLast());
        assertTrue(List.of(first.id(), second.id()).contains(result.getContent().getFirst().id()));
        assertTrue(List.of(first.id(), second.id()).contains(secondPage.getContent().getFirst().id()));
        assertTrue(result.getContent().stream().allMatch(item -> author.getId().equals(item.authorId())));
    }

    @Test
    void deleteRemovesSubscription() {
        SubscriptionReadDto created = subscriptionService.create(createDto(createUser(), createUser()));
        subscriptionService.delete(created.id());
        assertFalse(subscriptionRepository.existsById(created.id()));
    }
}
