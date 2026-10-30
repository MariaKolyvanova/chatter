package com.kolyvanova.chatter.like;

import com.kolyvanova.chatter.exception.LikeAlreadyExistsException;
import com.kolyvanova.chatter.exception.LikeNotFoundException;
import com.kolyvanova.chatter.exception.PostNotFoundException;
import com.kolyvanova.chatter.exception.UserNotFoundException;
import com.kolyvanova.chatter.post.Post;
import com.kolyvanova.chatter.post.PostCategory;
import com.kolyvanova.chatter.post.PostRepository;
import com.kolyvanova.chatter.post.PostStatus;
import com.kolyvanova.chatter.support.AbstractIntegrationTest;
import com.kolyvanova.chatter.user.User;
import com.kolyvanova.chatter.user.UserRepository;
import com.kolyvanova.chatter.user.UserRole;
import com.kolyvanova.chatter.user.UserStatus;
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
class LikeServiceTest extends AbstractIntegrationTest {

    @Autowired
    private LikeService likeService;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        likeRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User createUser() {
        String suffix = UUID.randomUUID().toString();
        User user = User.builder()
                .username("like_test_" + suffix)
                .email(suffix + "@mail.test")
                .passwordHash("test-password-hash")
                .displayName("Like test user")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        return userRepository.saveAndFlush(user);
    }

    private Post createPost(User author) {
        Post post = Post.builder()
                .author(author)
                .content("Post content")
                .category(PostCategory.GENERAL)
                .status(PostStatus.PUBLISHED)
                .build();

        return postRepository.saveAndFlush(post);
    }

    @Test
    void create() {
        User author = createUser();
        User user = createUser();
        Post post = createPost(author);

        LikeReadDto result = likeService.create(post.getId(), user.getId());

        assertNotNull(result.id());
        assertEquals(post.getId(), result.postId());
        assertEquals(user.getId(), result.userId());
    }

    @Test
    void createDuplicateLike() {
        User author = createUser();
        User user = createUser();
        Post post = createPost(author);
        likeService.create(post.getId(), user.getId());

        assertThrows(LikeAlreadyExistsException.class, () -> likeService.create(post.getId(), user.getId()));
    }

    @Test
    void createFailsWhenPostDoesNotExist() {
        User user = createUser();

        assertThrows(PostNotFoundException.class,
                () -> likeService.create(UUID.randomUUID(), user.getId()));
    }

    @Test
    void createFailsWhenUserDoesNotExist() {
        User author = createUser();
        Post post = createPost(author);

        assertThrows(UserNotFoundException.class,
                () -> likeService.create(post.getId(), UUID.randomUUID()));
    }

    @Test
    void findById() {
        User author = createUser();
        User user = createUser();
        Post post = createPost(author);
        LikeReadDto created = likeService.create(post.getId(), user.getId());

        LikeReadDto result = likeService.findById(created.id(), user.getId(), false);

        assertEquals(created.id(), result.id());
        assertEquals(created.postId(), result.postId());
        assertEquals(created.userId(), result.userId());
    }

    @Test
    void findByIdThrowsWhenLikeDoesNotExist() {
        assertThrows(LikeNotFoundException.class,
                () -> likeService.findById(UUID.randomUUID(), UUID.randomUUID(), false));
    }

    @Test
    void findAll() {
        User author = createUser();
        User firstUser = createUser();
        User secondUser = createUser();
        Post post = createPost(author);
        LikeReadDto first = likeService.create(post.getId(), firstUser.getId());
        LikeReadDto second = likeService.create(post.getId(), secondUser.getId());

        Page<LikeReadDto> firstPage = likeService.findAll(PageRequest.of(0, 1));
        Page<LikeReadDto> secondPage = likeService.findAll(PageRequest.of(1, 1));

        assertEquals(1, firstPage.getNumberOfElements());
        assertEquals(2, firstPage.getTotalElements());
        assertEquals(2, firstPage.getTotalPages());
        assertTrue(firstPage.hasNext());
        assertTrue(secondPage.isLast());
        assertTrue(List.of(first.id(), second.id()).contains(firstPage.getContent().getFirst().id()));
        assertTrue(List.of(first.id(), second.id()).contains(secondPage.getContent().getFirst().id()));
    }

    @Test
    void findAllByPostId() {
        User author = createUser();
        User firstUser = createUser();
        User secondUser = createUser();
        Post post = createPost(author);
        Post anotherPost = createPost(author);
        LikeReadDto first = likeService.create(post.getId(), firstUser.getId());
        LikeReadDto second = likeService.create(post.getId(), secondUser.getId());
        likeService.create(anotherPost.getId(), firstUser.getId());

        Page<LikeReadDto> result = likeService.findAllByPostId(post.getId(), author.getId(), false, PageRequest.of(0, 1));
        Page<LikeReadDto> secondPage = likeService.findAllByPostId(post.getId(), author.getId(), false, PageRequest.of(1, 1));

        assertEquals(1, result.getNumberOfElements());
        assertEquals(2, result.getTotalElements());
        assertTrue(result.hasNext());
        assertTrue(secondPage.isLast());
        assertTrue(result.getContent().stream().allMatch(like -> post.getId().equals(like.postId())));
        assertTrue(List.of(first.id(), second.id()).contains(result.getContent().getFirst().id()));
        assertTrue(List.of(first.id(), second.id()).contains(secondPage.getContent().getFirst().id()));
    }

    @Test
    void findAllByUserId() {
        User author = createUser();
        User user = createUser();
        Post firstPost = createPost(author);
        Post secondPost = createPost(author);
        LikeReadDto first = likeService.create(firstPost.getId(), user.getId());
        LikeReadDto second = likeService.create(secondPost.getId(), user.getId());

        Page<LikeReadDto> result = likeService.findAllByUserId(user.getId(), PageRequest.of(0, 1));
        Page<LikeReadDto> secondPage = likeService.findAllByUserId(user.getId(), PageRequest.of(1, 1));

        assertEquals(1, result.getNumberOfElements());
        assertEquals(2, result.getTotalElements());
        assertTrue(result.hasNext());
        assertTrue(secondPage.isLast());
        assertTrue(result.getContent().stream().allMatch(like -> user.getId().equals(like.userId())));
        assertTrue(List.of(first.id(), second.id()).contains(result.getContent().getFirst().id()));
        assertTrue(List.of(first.id(), second.id()).contains(secondPage.getContent().getFirst().id()));
    }

    @Test
    void deleteRemovesLike() {
        User author = createUser();
        User user = createUser();
        Post post = createPost(author);
        LikeReadDto created = likeService.create(post.getId(), user.getId());

        likeService.delete(user.getId(), post.getId());

        assertFalse(likeRepository.existsById(created.id()));
    }
}
