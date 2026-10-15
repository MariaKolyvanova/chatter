package com.kolyvanova.microblogging.post;

import com.kolyvanova.microblogging.exception.PostNotFoundException;
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

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    private int countUser = 0;

    @BeforeEach
    void cleanDatabase() {
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
                .build();
        countUser++;
        return userRepository.saveAndFlush(user);
    }

    private PostCreateDto createDto(UUID id) {
        return new PostCreateDto(id, "post", PostCategory.GENERAL);
    }

    @Test
    void create() {
        User author = createUser();
        PostCreateDto dto = createDto(author.getId());

        PostReadDto result = postService.create(dto);

        assertNotNull(result.id());
        assertEquals(author.getId(), result.authorId());
        assertEquals(dto.content(), result.content());
        assertEquals(dto.category(), result.category());
        assertEquals(PostStatus.PUBLISHED, result.status());

        Post savedPost = postRepository.findById(result.id()).orElseThrow();
        assertEquals(author.getId(), savedPost.getAuthor().getId());
    }

    @Test
    void createFailsWhenAuthorDoesNotExist() {
        PostCreateDto dto = createDto(UUID.randomUUID());

        assertThrows(UserNotFoundException.class, () -> postService.create(dto));
    }

    @Test
    void findById() {
        User author = createUser();
        PostReadDto created = postService.create(createDto(author.getId()));

        PostReadDto result = postService.findById(created.id());

        assertEquals(created.id(), result.id());
        assertEquals(created.content(), result.content());
        assertEquals(author.getId(), result.authorId());
    }

    @Test
    void findByIdThrowsWhenPostDoesNotExist() {
        assertThrows(PostNotFoundException.class,
                     () -> postService.findById(UUID.randomUUID()));
    }

    @Test
    void findAll() {
        User author = createUser();
        PostReadDto first = postService.create(createDto(author.getId()));
        PostReadDto second = postService.create(createDto(author.getId()));

        List<PostReadDto> result = postService.findAll();

        assertEquals(2, result.size());
        assertTrue(result.stream()
                           .map(PostReadDto::id)
                           .toList()
                           .containsAll(List.of(first.id(), second.id())));
    }

    @Test
    void findAllByAuthorId() {
        User author = createUser();
        User anotherAuthor = createUser();
        PostReadDto first = postService.create(createDto(author.getId()));
        PostReadDto second = postService.create(createDto(author.getId()));
        postService.create(createDto(anotherAuthor.getId()));

        List<PostReadDto> result = postService.findAllByAuthorId(author.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream()
                           .map(PostReadDto::id)
                           .toList()
                           .containsAll(List.of(first.id(), second.id())));
        assertTrue(result.stream()
                           .allMatch(post -> author.getId().equals(post.authorId())));
    }

    @Test
    void update() {
        User author = createUser();
        PostReadDto created = postService.create(createDto(author.getId()));
        PostUpdateDto dto = new PostUpdateDto("Updated content", PostCategory.EDUCATION);

        PostReadDto result = postService.update(created.id(), dto);

        assertEquals(dto.content(), result.content());
        assertEquals(dto.category(), result.category());
        assertEquals(created.authorId(), result.authorId());
        assertEquals(PostStatus.PUBLISHED, result.status());
    }

    @Test
    void delete() {
        User author = createUser();
        PostReadDto created = postService.create(createDto(author.getId()));

        postService.delete(created.id());

        assertFalse(postRepository.existsById(created.id()));
    }
}
