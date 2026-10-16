package com.kolyvanova.microblogging.like;

import com.kolyvanova.microblogging.exception.LikeAlreadyExistsException;
import com.kolyvanova.microblogging.exception.LikeNotFoundException;
import com.kolyvanova.microblogging.exception.PostNotFoundException;
import com.kolyvanova.microblogging.exception.UserNotFoundException;
import com.kolyvanova.microblogging.post.Post;
import com.kolyvanova.microblogging.post.PostCategory;
import com.kolyvanova.microblogging.post.PostRepository;
import com.kolyvanova.microblogging.post.PostStatus;
import com.kolyvanova.microblogging.support.AbstractIntegrationTest;
import com.kolyvanova.microblogging.user.User;
import com.kolyvanova.microblogging.user.UserRepository;
import com.kolyvanova.microblogging.user.UserRole;
import com.kolyvanova.microblogging.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

    private LikeCreateDto createDto(Post post, User user) {
        return new LikeCreateDto(post.getId(), user.getId());
    }

    @Test
    void create() {
        User author = createUser();
        User user = createUser();
        Post post = createPost(author);

        LikeReadDto result = likeService.create(createDto(post, user));

        assertNotNull(result.id());
        assertEquals(post.getId(), result.postId());
        assertEquals(user.getId(), result.userId());
        assertNotNull(result.createdAt());
    }

    @Test
    void createDuplicateLike() {
        User author = createUser();
        User user = createUser();
        Post post = createPost(author);
        LikeCreateDto dto = createDto(post, user);
        likeService.create(dto);

        assertThrows(LikeAlreadyExistsException.class, () -> likeService.create(dto));
    }

    @Test
    void createFailsWhenPostDoesNotExist() {
        User user = createUser();

        assertThrows(PostNotFoundException.class,
                () -> likeService.create(new LikeCreateDto(UUID.randomUUID(), user.getId())));
    }

    @Test
    void createFailsWhenUserDoesNotExist() {
        User author = createUser();
        Post post = createPost(author);

        assertThrows(UserNotFoundException.class,
                () -> likeService.create(new LikeCreateDto(post.getId(), UUID.randomUUID())));
    }

    @Test
    void findById() {
        User author = createUser();
        User user = createUser();
        Post post = createPost(author);
        LikeReadDto created = likeService.create(createDto(post, user));

        LikeReadDto result = likeService.findById(created.id());

        assertEquals(created.id(), result.id());
        assertEquals(created.postId(), result.postId());
        assertEquals(created.userId(), result.userId());
    }

    @Test
    void findByIdThrowsWhenLikeDoesNotExist() {
        assertThrows(LikeNotFoundException.class, () -> likeService.findById(UUID.randomUUID()));
    }

    @Test
    void findAll() {
        User author = createUser();
        User firstUser = createUser();
        User secondUser = createUser();
        Post post = createPost(author);
        LikeReadDto first = likeService.create(createDto(post, firstUser));
        LikeReadDto second = likeService.create(createDto(post, secondUser));

        List<LikeReadDto> result = likeService.findAll();

        assertEquals(2, result.size());
        assertTrue(result.stream().map(LikeReadDto::id).toList()
                .containsAll(List.of(first.id(), second.id())));
    }

    @Test
    void findAllByPostId() {
        User author = createUser();
        User firstUser = createUser();
        User secondUser = createUser();
        Post post = createPost(author);
        Post anotherPost = createPost(author);
        LikeReadDto first = likeService.create(createDto(post, firstUser));
        LikeReadDto second = likeService.create(createDto(post, secondUser));
        likeService.create(createDto(anotherPost, firstUser));

        List<LikeReadDto> result = likeService.findAllByPostId(post.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().map(LikeReadDto::id).toList()
                .containsAll(List.of(first.id(), second.id())));
        assertTrue(result.stream().allMatch(like -> post.getId().equals(like.postId())));
    }

    @Test
    void findAllByUserId() {
        User author = createUser();
        User user = createUser();
        Post firstPost = createPost(author);
        Post secondPost = createPost(author);
        LikeReadDto first = likeService.create(createDto(firstPost, user));
        LikeReadDto second = likeService.create(createDto(secondPost, user));

        List<LikeReadDto> result = likeService.findAllByUserId(user.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().map(LikeReadDto::id).toList()
                .containsAll(List.of(first.id(), second.id())));
        assertTrue(result.stream().allMatch(like -> user.getId().equals(like.userId())));
    }

    @Test
    void deleteRemovesLike() {
        User author = createUser();
        User user = createUser();
        Post post = createPost(author);
        LikeReadDto created = likeService.create(createDto(post, user));

        likeService.delete(created.id());

        assertFalse(likeRepository.existsById(created.id()));
    }
}
