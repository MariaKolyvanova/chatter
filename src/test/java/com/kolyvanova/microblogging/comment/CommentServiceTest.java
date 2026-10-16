package com.kolyvanova.microblogging.comment;

import com.kolyvanova.microblogging.exception.CommentNotFoundException;
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
class CommentServiceTest extends AbstractIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User createUser() {
        String suffix = UUID.randomUUID().toString();
        User user = User.builder()
                .username("comment_test_" + suffix)
                .email(suffix + "@mail.test")
                .passwordHash("test-password-hash")
                .displayName("Comment test user")
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

    private CommentCreateDto createDto(Post post, User author, String content) {
        return new CommentCreateDto(post.getId(), author.getId(), content);
    }

    @Test
    void create() {
        User author = createUser();
        Post post = createPost(author);
        CommentCreateDto dto = createDto(post, author, "My first comment");

        CommentReadDto result = commentService.create(dto);

        assertNotNull(result.id());
        assertEquals(post.getId(), result.postId());
        assertEquals(author.getId(), result.authorId());
        assertEquals(dto.content(), result.content());

        Comment savedComment = commentRepository.findById(result.id()).orElseThrow();
        assertEquals(post.getId(), savedComment.getPost().getId());
        assertEquals(author.getId(), savedComment.getAuthor().getId());
    }

    @Test
    void createFailsWhenAuthorDoesNotExist() {
        User postAuthor = createUser();
        Post post = createPost(postAuthor);
        CommentCreateDto dto = new CommentCreateDto(post.getId(), UUID.randomUUID(), "Comment");

        assertThrows(UserNotFoundException.class, () -> commentService.create(dto));
    }

    @Test
    void createFailsWhenPostDoesNotExist() {
        User author = createUser();
        CommentCreateDto dto = new CommentCreateDto(UUID.randomUUID(), author.getId(), "Comment");

        assertThrows(PostNotFoundException.class, () -> commentService.create(dto));
    }

    @Test
    void findById() {
        User author = createUser();
        Post post = createPost(author);
        CommentReadDto created = commentService.create(createDto(post, author, "Comment"));

        CommentReadDto result = commentService.findById(created.id());

        assertEquals(created.id(), result.id());
        assertEquals(created.content(), result.content());
        assertEquals(post.getId(), result.postId());
        assertEquals(author.getId(), result.authorId());
    }

    @Test
    void findByIdThrowsWhenCommentDoesNotExist() {
        assertThrows(CommentNotFoundException.class,
                     () -> commentService.findById(UUID.randomUUID()));
    }

    @Test
    void findAll() {
        User author = createUser();
        Post post = createPost(author);
        CommentReadDto first = commentService.create(createDto(post, author, "First comment"));
        CommentReadDto second = commentService.create(createDto(post, author, "Second comment"));

        List<CommentReadDto> result = commentService.findAll();

        assertEquals(2, result.size());
        assertTrue(result.stream().map(CommentReadDto::id).toList()
                .containsAll(List.of(first.id(), second.id())));
    }

    @Test
    void findAllByPostId() {
        User author = createUser();
        Post post = createPost(author);
        Post anotherPost = createPost(author);
        CommentReadDto first = commentService.create(createDto(post, author, "First comment"));
        CommentReadDto second = commentService.create(createDto(post, author, "Second comment"));
        commentService.create(createDto(anotherPost, author, "Comment on another post"));

        List<CommentReadDto> result = commentService.findAllByPostId(post.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().map(CommentReadDto::id).toList()
                .containsAll(List.of(first.id(), second.id())));
        assertTrue(result.stream().allMatch(comment -> post.getId().equals(comment.postId())));
    }

    @Test
    void findAllByAuthorId() {
        User author = createUser();
        User anotherAuthor = createUser();
        Post post = createPost(author);
        CommentReadDto first = commentService.create(createDto(post, author, "First comment"));
        CommentReadDto second = commentService.create(createDto(post, author, "Second comment"));
        commentService.create(createDto(post, anotherAuthor, "Another author's comment"));

        List<CommentReadDto> result = commentService.findAllByAuthorId(author.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().map(CommentReadDto::id).toList()
                .containsAll(List.of(first.id(), second.id())));
        assertTrue(result.stream().allMatch(comment -> author.getId().equals(comment.authorId())));
    }

    @Test
    void update() {
        User author = createUser();
        Post post = createPost(author);
        CommentReadDto created = commentService.create(createDto(post, author, "Old comment"));
        CommentUpdateDto dto = new CommentUpdateDto("Updated comment");

        CommentReadDto result = commentService.update(created.id(), dto);

        assertEquals(dto.content(), result.content());
        assertEquals(created.postId(), result.postId());
        assertEquals(created.authorId(), result.authorId());
    }

    @Test
    void deleteRemovesComment() {
        User author = createUser();
        Post post = createPost(author);
        CommentReadDto created = commentService.create(createDto(post, author, "Comment to delete"));

        commentService.delete(created.id());

        assertFalse(commentRepository.existsById(created.id()));
    }
}
