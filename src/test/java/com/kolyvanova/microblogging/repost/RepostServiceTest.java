package com.kolyvanova.microblogging.repost;

import com.kolyvanova.microblogging.exception.PostNotFoundException;
import com.kolyvanova.microblogging.exception.RepostAlreadyExistsException;
import com.kolyvanova.microblogging.exception.RepostNotFoundException;
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
class RepostServiceTest extends AbstractIntegrationTest {

    @Autowired private RepostService repostService;
    @Autowired private RepostRepository repostRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        repostRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User createUser() {
        String suffix = UUID.randomUUID().toString();
        return userRepository.saveAndFlush(User.builder()
                .username("repost_test_" + suffix).email(suffix + "@mail.test")
                .passwordHash("test-password-hash").displayName("Repost test user")
                .role(UserRole.USER).status(UserStatus.ACTIVE).build());
    }

    private Post createPost(User author) {
        return postRepository.saveAndFlush(Post.builder().author(author).content("Post content")
                .category(PostCategory.GENERAL).status(PostStatus.PUBLISHED).build());
    }

    private RepostCreateDto createDto(Post post, User user) {
        return new RepostCreateDto(post.getId(), user.getId());
    }

    @Test
    void create() {
        User author = createUser();
        User user = createUser();
        Post post = createPost(author);
        RepostReadDto result = repostService.create(createDto(post, user));
        assertNotNull(result.id());
        assertEquals(post.getId(), result.postId());
        assertEquals(user.getId(), result.userId());
    }

    @Test
    void createDuplicateRepost() {
        User author = createUser();
        User user = createUser();
        Post post = createPost(author);
        RepostCreateDto dto = createDto(post, user);
        repostService.create(dto);
        assertThrows(RepostAlreadyExistsException.class, () -> repostService.create(dto));
    }

    @Test
    void createFailsWhenPostDoesNotExist() {
        User user = createUser();
        assertThrows(PostNotFoundException.class,
                () -> repostService.create(new RepostCreateDto(UUID.randomUUID(), user.getId())));
    }

    @Test
    void createFailsWhenUserDoesNotExist() {
        User author = createUser();
        Post post = createPost(author);
        assertThrows(UserNotFoundException.class,
                () -> repostService.create(new RepostCreateDto(post.getId(), UUID.randomUUID())));
    }

    @Test
    void findById() {
        User author = createUser();
        User user = createUser();
        RepostReadDto created = repostService.create(createDto(createPost(author), user));
        RepostReadDto result = repostService.findById(created.id());
        assertEquals(created.id(), result.id());
        assertEquals(created.postId(), result.postId());
        assertEquals(created.userId(), result.userId());
    }

    @Test
    void findByIdThrowsWhenRepostDoesNotExist() {
        assertThrows(RepostNotFoundException.class, () -> repostService.findById(UUID.randomUUID()));
    }

    @Test
    void findAll() {
        User author = createUser();
        Post post = createPost(author);
        RepostReadDto first = repostService.create(createDto(post, createUser()));
        RepostReadDto second = repostService.create(createDto(post, createUser()));
        List<RepostReadDto> result = repostService.findAll();
        assertEquals(2, result.size());
        assertTrue(result.stream().map(RepostReadDto::id).toList().containsAll(List.of(first.id(), second.id())));
    }

    @Test
    void findAllByPostId() {
        User author = createUser();
        Post post = createPost(author);
        Post anotherPost = createPost(author);
        RepostReadDto first = repostService.create(createDto(post, createUser()));
        RepostReadDto second = repostService.create(createDto(post, createUser()));
        repostService.create(createDto(anotherPost, createUser()));
        List<RepostReadDto> result = repostService.findAllByPostId(post.getId());
        assertEquals(2, result.size());
        assertTrue(result.stream().map(RepostReadDto::id).toList().containsAll(List.of(first.id(), second.id())));
        assertTrue(result.stream().allMatch(repost -> post.getId().equals(repost.postId())));
    }

    @Test
    void findAllByUserId() {
        User author = createUser();
        User user = createUser();
        RepostReadDto first = repostService.create(createDto(createPost(author), user));
        RepostReadDto second = repostService.create(createDto(createPost(author), user));
        List<RepostReadDto> result = repostService.findAllByUserId(user.getId());
        assertEquals(2, result.size());
        assertTrue(result.stream().map(RepostReadDto::id).toList().containsAll(List.of(first.id(), second.id())));
        assertTrue(result.stream().allMatch(repost -> user.getId().equals(repost.userId())));
    }

    @Test
    void deleteRemovesRepost() {
        User author = createUser();
        RepostReadDto created = repostService.create(createDto(createPost(author), createUser()));
        repostService.delete(created.id());
        assertFalse(repostRepository.existsById(created.id()));
    }
}
