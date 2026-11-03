package com.kolyvanova.chatter.post;

import com.kolyvanova.chatter.support.AbstractControllerTest;
import com.kolyvanova.chatter.user.User;
import com.kolyvanova.chatter.user.UserReadDto;
import com.kolyvanova.chatter.user.UserRepository;
import com.kolyvanova.chatter.user.UserRole;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostRestControllerTest extends AbstractControllerTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void feedRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required"));
    }

    @Test
    void createRejectsBlankContent() throws Exception {
        mockMvc.perform(post("/api/v1/posts")
                        .with(asUser(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content": "", "category": "GENERAL"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRejectsContentLongerThan280Characters() throws Exception {
        mockMvc.perform(post("/api/v1/posts")
                        .with(asUser(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content": "%s", "category": "GENERAL"}
                                """.formatted("a".repeat(281))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateOfAnotherAuthorsPostIsForbidden() throws Exception {
        UserReadDto author = createUser("author");
        UserReadDto other = createUser("other");
        PostReadDto created = postService.create(new PostCreateDto("post", PostCategory.GENERAL, null), author.id());

        mockMvc.perform(put("/api/v1/posts/{id}", created.id())
                        .with(asUser(other.id()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content": "changed", "category": "GENERAL"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void hiddenPostIsNotFoundForAnotherUser() throws Exception {
        UserReadDto author = createUser("author");
        UserReadDto other = createUser("other");
        PostReadDto created = postService.create(new PostCreateDto("post", PostCategory.GENERAL, null), author.id());
        Post post = postRepository.findById(created.id()).orElseThrow();
        post.setStatus(PostStatus.HIDDEN);
        postRepository.saveAndFlush(post);

        mockMvc.perform(get("/api/v1/posts/{id}", created.id()).with(asUser(other.id())))
                .andExpect(status().isNotFound());
    }

    @Test
    void authorCanAddAnImageAndAHiddenPostHidesIt() throws Exception {
        UserReadDto author = createUser("author");
        UserReadDto other = createUser("other");
        PostReadDto created = postService.create(new PostCreateDto("post", PostCategory.GENERAL, null), author.id());
        byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
        MockMultipartFile image = new MockMultipartFile("file", "post.jpg", "image/jpeg", jpeg);

        mockMvc.perform(multipart("/api/v1/posts/{id}/image", created.id())
                        .file(image)
                        .with(asUser(other.id())))
                .andExpect(status().isForbidden());

        mockMvc.perform(multipart("/api/v1/posts/{id}/image", created.id())
                        .file(image)
                        .with(asUser(author.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageKey").exists())
                .andExpect(jsonPath("$.viewCount").value(0));

        mockMvc.perform(get("/api/v1/posts/{id}/image", created.id()).with(asUser(other.id())))
                .andExpect(status().isOk())
                .andExpect(content().bytes(jpeg));
        assertEquals(0, postRepository.findById(created.id()).orElseThrow().getViewCount());

        Post post = postRepository.findById(created.id()).orElseThrow();
        post.setStatus(PostStatus.HIDDEN);
        postRepository.saveAndFlush(post);

        mockMvc.perform(get("/api/v1/posts/{id}/image", created.id()).with(asUser(other.id())))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/posts/{id}/image", created.id()).with(asUser(author.id())))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/posts/{id}/image", created.id()).with(asUser(other.id())))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/posts/{id}/image", created.id()).with(asUser(author.id())))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/posts/{id}", created.id()).with(asUser(author.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageKey").value(nullValue()));
        mockMvc.perform(get("/api/v1/posts/{id}/image", created.id()).with(asUser(author.id())))
                .andExpect(status().isNotFound());
    }

    @Test
    void listsPostsByCategory() throws Exception {
        UserReadDto author = createUser("author");
        PostReadDto nature = postService.create(new PostCreateDto("nature", PostCategory.NATURE, null), author.id());
        postService.create(new PostCreateDto("general", PostCategory.GENERAL, null), author.id());

        mockMvc.perform(get("/api/v1/posts").param("category", "NATURE").with(asUser(author.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem(nature.id().toString())))
                .andExpect(jsonPath("$.content[*].category", not(hasItem("GENERAL"))));
    }

    @Test
    void readingWithoutCountViewLeavesTheCounter() throws Exception {
        UserReadDto author = createUser("author");
        UserReadDto reader = createUser("reader");
        PostReadDto created = postService.create(new PostCreateDto("post", PostCategory.GENERAL, null), author.id());

        mockMvc.perform(get("/api/v1/posts/{id}", created.id())
                        .param("countView", "false")
                        .with(asUser(reader.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewCount").value(0));
        assertEquals(0, postRepository.findById(created.id()).orElseThrow().getViewCount());

        mockMvc.perform(get("/api/v1/posts/{id}", created.id()).with(asUser(reader.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewCount").value(1));
    }

    @Test
    void listsRepliesWithoutCountingAViewAndSkipsAHiddenReply() throws Exception {
        UserReadDto author = createUser("author");
        UserReadDto replier = createUser("replier");
        UserReadDto reader = createUser("reader");
        PostReadDto parent = postService.create(new PostCreateDto("parent", PostCategory.GENERAL, null), author.id());
        PostReadDto reply = postService.create(
                new PostCreateDto("reply", PostCategory.GENERAL, parent.id()), replier.id());
        PostReadDto hidden = postService.create(
                new PostCreateDto("hidden reply", PostCategory.GENERAL, parent.id()), replier.id());
        Post hiddenReply = postRepository.findById(hidden.id()).orElseThrow();
        hiddenReply.setStatus(PostStatus.HIDDEN);
        postRepository.saveAndFlush(hiddenReply);

        mockMvc.perform(get("/api/v1/posts/{id}/replies", parent.id()).with(asUser(reader.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem(reply.id().toString())))
                .andExpect(jsonPath("$.content[*].id", not(hasItem(hidden.id().toString()))));
        assertEquals(0, postRepository.findById(parent.id()).orElseThrow().getViewCount());

        Post parentPost = postRepository.findById(parent.id()).orElseThrow();
        parentPost.setStatus(PostStatus.HIDDEN);
        postRepository.saveAndFlush(parentPost);

        mockMvc.perform(get("/api/v1/posts/{id}/replies", parent.id()).with(asUser(reader.id())))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/posts/{id}/replies", parent.id()).with(asUser(author.id())))
                .andExpect(status().isOk());
    }

    @Test
    void listsAnAuthorsReplies() throws Exception {
        UserReadDto author = createUser("author");
        UserReadDto reader = createUser("reader");
        PostReadDto parent = postService.create(new PostCreateDto("parent", PostCategory.GENERAL, null), author.id());
        PostReadDto reply = postService.create(
                new PostCreateDto("reply", PostCategory.GENERAL, parent.id()), author.id());

        mockMvc.perform(get("/api/v1/posts/by-author")
                        .param("authorId", author.id().toString())
                        .param("replies", "true")
                        .with(asUser(reader.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem(reply.id().toString())))
                .andExpect(jsonPath("$.content[*].id", not(hasItem(parent.id().toString()))));
    }

    @Test
    void feedReturnsFollowedPostsAndMineReturnsOwnPosts() throws Exception {
        UserReadDto follower = createUser("follower");
        UserReadDto author = createUser("author");
        PostReadDto followed = postService.create(new PostCreateDto("followed", PostCategory.NEWS, null), author.id());
        postService.create(new PostCreateDto("reply", PostCategory.NEWS, followed.id()), author.id());
        PostReadDto own = postService.create(new PostCreateDto("own", PostCategory.GENERAL, null), follower.id());

        mockMvc.perform(post("/api/v1/subscriptions")
                        .param("authorId", author.id().toString())
                        .with(asUser(follower.id())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/posts/feed").with(asUser(follower.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem(followed.id().toString())))
                .andExpect(jsonPath("$.content[*].id", not(hasItem(own.id().toString()))));

        mockMvc.perform(get("/api/v1/posts/me").with(asUser(follower.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem(own.id().toString())));
    }

    @Test
    void moderatorCanHideAndShowAndListPostsForModeration() throws Exception {
        UserReadDto author = createUser("author");
        UserReadDto moderator = createUser("moderator");
        User stored = userRepository.findById(moderator.id()).orElseThrow();
        stored.setRole(UserRole.MODERATOR);
        userRepository.saveAndFlush(stored);
        PostReadDto created = postService.create(new PostCreateDto("post", PostCategory.GENERAL, null), author.id());

        mockMvc.perform(get("/api/v1/posts/moderation").with(asUser(author.id())))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/v1/posts/{id}/hide", created.id()).with(asUser(author.id())))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/posts/{id}/hide", created.id()).with(asUser(moderator.id())))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/posts/moderation").param("status", "HIDDEN").with(asUser(moderator.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem(created.id().toString())));

        PostReadDto published = postService.create(new PostCreateDto("visible", PostCategory.GENERAL, null), author.id());
        mockMvc.perform(get("/api/v1/posts/moderation").with(asUser(moderator.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem(created.id().toString())))
                .andExpect(jsonPath("$.content[*].id", hasItem(published.id().toString())));

        mockMvc.perform(put("/api/v1/posts/{id}/show", created.id()).with(asUser(moderator.id())))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/posts/{id}", created.id()).with(asUser(author.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void authorCanDeleteAPost() throws Exception {
        UserReadDto author = createUser("author");
        UserReadDto other = createUser("other");
        PostReadDto created = postService.create(new PostCreateDto("post", PostCategory.GENERAL, null), author.id());

        mockMvc.perform(delete("/api/v1/posts/{id}", created.id()).with(asUser(other.id())))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/posts/{id}", created.id()).with(asUser(author.id())))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/posts/{id}", created.id()).with(asUser(author.id())))
                .andExpect(status().isNotFound());
    }
}
