package com.kolyvanova.chatter.post;

import com.kolyvanova.chatter.exception.PostNotFoundException;
import com.kolyvanova.chatter.exception.UserNotFoundException;
import com.kolyvanova.chatter.subscription.Subscription;
import com.kolyvanova.chatter.subscription.SubscriptionRepository;
import com.kolyvanova.chatter.support.AbstractIntegrationTest;
import com.kolyvanova.chatter.user.User;
import com.kolyvanova.chatter.user.UserRepository;
import com.kolyvanova.chatter.user.UserRole;
import com.kolyvanova.chatter.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PostServiceTest extends AbstractIntegrationTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private int countUser = 0;

    @BeforeEach
    void cleanDatabase() {
        subscriptionRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User createUser() {
        User user = User.builder()
                .username("user" + countUser)
                .email("user" + countUser + "@mail.test")
                .passwordHash("test-password-hash")
                .displayName("Post test user")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();
        countUser++;
        return userRepository.saveAndFlush(user);
    }

    private PostCreateDto createDto(String content) {
        return new PostCreateDto(content, PostCategory.GENERAL, null);
    }

    @Test
    void create() {
        User author = createUser();
        PostCreateDto dto = createDto("post");

        PostReadDto result = postService.create(dto, author.getId());

        assertNotNull(result.id());
        assertEquals(author.getId(), result.authorId());
        assertEquals(dto.content(), result.content());
        assertEquals(dto.category(), result.category());
        assertEquals(PostStatus.PUBLISHED, result.status());
        assertNull(result.replyToId());
        assertEquals(result.id(), result.conversationId());

        Post savedPost = postRepository.findById(result.id()).orElseThrow();
        assertEquals(author.getId(), savedPost.getAuthor().getId());
    }

    @Test
    void createFailsWhenAuthorDoesNotExist() {
        PostCreateDto dto = createDto("post");

        assertThrows(UserNotFoundException.class, () -> postService.create(dto, UUID.randomUUID()));
    }

    @Test
    void findById() {
        User author = createUser();
        PostReadDto created = postService.create(createDto("post"), author.getId());

        PostReadDto result = postService.findById(created.id(), author.getId(), false);

        assertEquals(created.id(), result.id());
        assertEquals(created.content(), result.content());
        assertEquals(author.getId(), result.authorId());
    }

    @Test
    void findByIdIncrementsViewCount() {
        User author = createUser();
        User other = createUser();
        PostReadDto created = postService.create(createDto("post"), author.getId());

        assertEquals(0, created.viewCount());
        postService.findAll(PageRequest.of(0, 10));
        assertEquals(0, postRepository.findById(created.id()).orElseThrow().getViewCount());

        assertEquals(1, postService.findById(created.id(), other.getId(), false).viewCount());
        assertEquals(2, postService.findById(created.id(), author.getId(), false).viewCount());
        assertEquals(2, postRepository.findById(created.id()).orElseThrow().getViewCount());

        Post post = postRepository.findById(created.id()).orElseThrow();
        post.setStatus(PostStatus.HIDDEN);
        postRepository.saveAndFlush(post);

        assertThrows(PostNotFoundException.class,
                () -> postService.findById(created.id(), other.getId(), false));
        assertEquals(2, postRepository.findById(created.id()).orElseThrow().getViewCount());
    }

    @Test
    void findByIdThrowsWhenPostDoesNotExist() {
        assertThrows(PostNotFoundException.class,
                     () -> postService.findById(UUID.randomUUID(), UUID.randomUUID(), false));
    }

    @Test
    void findAllPublished() {
        User author = createUser();
        PostReadDto first = postService.create(createDto("first"), author.getId());
        PostReadDto second = postService.create(createDto("second"), author.getId());

        Page<PostReadDto> firstPage = postService.findAll(PageRequest.of(0, 1, Sort.by("content").ascending()));
        Page<PostReadDto> secondPage = postService.findAll(PageRequest.of(1, 1, Sort.by("content").ascending()));

        assertEquals(1, firstPage.getNumberOfElements());
        assertEquals(2, firstPage.getTotalElements());
        assertEquals(2, firstPage.getTotalPages());
        assertTrue(firstPage.hasNext());
        assertTrue(secondPage.isLast());
        assertTrue(List.of(first.id(), second.id()).contains(firstPage.getContent().getFirst().id()));
        assertTrue(List.of(first.id(), second.id()).contains(secondPage.getContent().getFirst().id()));
    }

    @Test
    void findAllPublishedByAuthorId() {
        User author = createUser();
        User anotherAuthor = createUser();
        PostReadDto first = postService.create(createDto("first"), author.getId());
        PostReadDto second = postService.create(createDto("second"), author.getId());
        postService.create(createDto("other"), anotherAuthor.getId());

        Page<PostReadDto> firstPage = postService.findAllByAuthorId(author.getId(),
                PageRequest.of(0, 1, Sort.by("content").ascending()));
        Page<PostReadDto> secondPage = postService.findAllByAuthorId(author.getId(),
                PageRequest.of(1, 1, Sort.by("content").ascending()));

        assertEquals(1, firstPage.getNumberOfElements());
        assertEquals(2, firstPage.getTotalElements());
        assertTrue(firstPage.hasNext());
        assertTrue(secondPage.isLast());
        assertTrue(List.of(first.id(), second.id()).contains(firstPage.getContent().getFirst().id()));
        assertTrue(List.of(first.id(), second.id()).contains(secondPage.getContent().getFirst().id()));
        assertTrue(firstPage.getContent().stream().allMatch(post -> author.getId().equals(post.authorId())));
    }

    @Test
    void update() {
        User author = createUser();
        PostReadDto created = postService.create(createDto("post"), author.getId());
        PostUpdateDto dto = new PostUpdateDto("Updated content", PostCategory.EDUCATION);

        PostReadDto result = postService.update(created.id(), author.getId(), dto);

        assertEquals(dto.content(), result.content());
        assertEquals(dto.category(), result.category());
        assertEquals(created.authorId(), result.authorId());
        assertEquals(PostStatus.PUBLISHED, result.status());
        assertFalse(created.changed());
        assertTrue(result.changed());
    }

    @Test
    void createWithoutCategoryUsesOtherAndReplyKeepsTheParentCategory() {
        User author = createUser();
        PostReadDto root = postService.create(new PostCreateDto("root", null, null), author.getId());
        PostReadDto reply = postService.create(
                new PostCreateDto("reply", PostCategory.SPORTS, root.id()), author.getId());

        assertEquals(PostCategory.OTHER, root.category());
        assertEquals(PostCategory.OTHER, reply.category());
    }

    @Test
    void createReplyKeepsTheParentConversationAndStaysOutOfTheFeed() {
        User author = createUser();
        PostReadDto parent = postService.create(createDto("parent"), author.getId());

        PostReadDto reply = postService.create(
                new PostCreateDto("reply", PostCategory.GENERAL, parent.id()), author.getId());

        assertEquals(parent.id(), reply.replyToId());
        assertEquals(parent.id(), reply.conversationId());
        assertEquals(parent.category(), reply.category());
        assertEquals(List.of(parent.id()), postService.findAll(PageRequest.of(0, 10))
                .map(PostReadDto::id).getContent());
    }

    @Test
    void updateAndDeleteFailForAnotherAuthor() {
        User author = createUser();
        User other = createUser();
        PostReadDto created = postService.create(createDto("post"), author.getId());
        PostUpdateDto dto = new PostUpdateDto("changed", PostCategory.GENERAL);

        assertThrows(AccessDeniedException.class, () -> postService.update(created.id(), other.getId(), dto));
        assertThrows(AccessDeniedException.class, () -> postService.delete(created.id(), other.getId()));
        assertTrue(postRepository.existsById(created.id()));
    }

    @Test
    void hideAndShowChangeStatus() {
        User author = createUser();
        User moderator = createUser();
        moderator.setRole(UserRole.MODERATOR);
        userRepository.saveAndFlush(moderator);
        PostReadDto created = postService.create(createDto("post"), author.getId());

        assertThrows(AccessDeniedException.class, () -> postService.hide(created.id(), author.getId()));

        postService.hide(created.id(), moderator.getId());
        assertEquals(PostStatus.HIDDEN, postService.findById(created.id(), author.getId(), false).status());

        postService.show(created.id(), moderator.getId());
        assertEquals(PostStatus.PUBLISHED, postService.findById(created.id(), author.getId(), false).status());
    }

    @Test
    void moderatorCanBrowseHiddenPostsAndReplies() {
        User author = createUser();
        User moderator = createUser();
        moderator.setRole(UserRole.MODERATOR);
        userRepository.saveAndFlush(moderator);
        PostReadDto published = postService.create(createDto("public"), author.getId());
        PostReadDto hidden = postService.create(createDto("secret"), author.getId());
        PostReadDto reply = postService.create(new PostCreateDto("reply", PostCategory.GENERAL, published.id()), author.getId());
        postService.hide(hidden.id(), moderator.getId());
        Pageable page = PageRequest.of(0, 10);

        assertThrows(AccessDeniedException.class,
                () -> postService.findAuthorPosts(author.getId(), null, true, author.getId(), page));
        assertThrows(AccessDeniedException.class,
                () -> postService.findForModeration(author.getId(), PostStatus.HIDDEN, page));

        assertEquals(List.of(published.id()), postService.findAllByAuthorId(author.getId(), page).map(PostReadDto::id).getContent());
        assertTrue(postService.findAuthorPosts(author.getId(), null, true, moderator.getId(), page)
                .map(PostReadDto::id).getContent().containsAll(List.of(published.id(), hidden.id())));
        assertEquals(List.of(hidden.id()), postService.findAuthorPosts(author.getId(), PostStatus.HIDDEN, false, moderator.getId(), page)
                .map(PostReadDto::id).getContent());

        List<UUID> visible = postService.findForModeration(moderator.getId(), PostStatus.PUBLISHED, page)
                .map(PostReadDto::id).getContent();
        assertTrue(visible.contains(published.id()));
        assertTrue(visible.contains(reply.id()));
        assertFalse(visible.contains(hidden.id()));
        assertEquals(List.of(hidden.id()), postService.findForModeration(moderator.getId(), PostStatus.HIDDEN, page)
                .map(PostReadDto::id).getContent());
    }

    @Test
    void delete() {
        User author = createUser();
        PostReadDto created = postService.create(createDto("post"), author.getId());

        postService.delete(created.id(), author.getId());

        assertFalse(postRepository.existsById(created.id()));
    }

    @Test
    void hiddenPostIsVisibleToAuthorAndModeratorOnly() {
        User author = createUser();
        User other = createUser();
        PostReadDto created = postService.create(createDto("hidden"), author.getId());
        Post post = postRepository.findById(created.id()).orElseThrow();
        post.setStatus(PostStatus.HIDDEN);
        postRepository.saveAndFlush(post);

        assertTrue(postService.findAll(PageRequest.of(0, 10)).isEmpty());
        assertTrue(postService.findAllByAuthorId(author.getId(), PageRequest.of(0, 10)).isEmpty());
        assertEquals(1, postService.findMine(author.getId(), PageRequest.of(0, 10)).getTotalElements());
        assertEquals(PostStatus.HIDDEN, postService.findById(created.id(), author.getId(), false).status());
        assertEquals(PostStatus.HIDDEN, postService.findById(created.id(), other.getId(), true).status());
        assertThrows(PostNotFoundException.class,
                () -> postService.findById(created.id(), other.getId(), false));
    }

    @Test
    void findFeedReturnsPublishedPostsOfFollowedAuthors() {
        User subscriber = createUser();
        User followed = createUser();
        User stranger = createUser();
        subscriptionRepository.saveAndFlush(Subscription.builder()
                .subscriber(subscriber)
                .author(followed)
                .build());

        PostReadDto followedPost = postService.create(createDto("followed"), followed.getId());
        postService.create(new PostCreateDto("reply", PostCategory.GENERAL, followedPost.id()), followed.getId());
        PostReadDto hidden = postService.create(createDto("hidden"), followed.getId());
        Post hiddenPost = postRepository.findById(hidden.id()).orElseThrow();
        hiddenPost.setStatus(PostStatus.HIDDEN);
        postRepository.saveAndFlush(hiddenPost);
        postService.create(createDto("stranger"), stranger.getId());
        postService.create(createDto("own"), subscriber.getId());

        Page<PostReadDto> feed = postService.findFeed(subscriber.getId(), PageRequest.of(0, 10));
        Page<PostReadDto> emptyFeed = postService.findFeed(stranger.getId(), PageRequest.of(0, 10));

        assertEquals(1, feed.getTotalElements());
        assertEquals(followedPost.id(), feed.getContent().getFirst().id());
        assertTrue(emptyFeed.isEmpty());
    }
}
