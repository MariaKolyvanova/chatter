package com.kolyvanova.chatter.post;

import com.kolyvanova.chatter.support.AbstractControllerTest;
import com.kolyvanova.chatter.user.UserReadDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

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
    }
}
