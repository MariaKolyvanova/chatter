package com.kolyvanova.chatter.repost;

import com.jayway.jsonpath.JsonPath;
import com.kolyvanova.chatter.post.Post;
import com.kolyvanova.chatter.post.PostCategory;
import com.kolyvanova.chatter.post.PostCreateDto;
import com.kolyvanova.chatter.post.PostReadDto;
import com.kolyvanova.chatter.post.PostRepository;
import com.kolyvanova.chatter.post.PostService;
import com.kolyvanova.chatter.post.PostStatus;
import com.kolyvanova.chatter.support.AbstractControllerTest;
import com.kolyvanova.chatter.user.UserReadDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RepostRestControllerTest extends AbstractControllerTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Test
    void createsListsAndDeletesARepost() throws Exception {
        UserReadDto author = createUser("author");
        UserReadDto user = createUser("reader");
        UserReadDto other = createUser("other");
        PostReadDto created = postService.create(new PostCreateDto("post", PostCategory.GENERAL, null), author.id());

        String body = mockMvc.perform(post("/api/v1/reposts")
                        .param("postId", created.id().toString())
                        .with(asUser(user.id())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.postId").value(created.id().toString()))
                .andExpect(jsonPath("$.userId").value(user.id().toString()))
                .andReturn().getResponse().getContentAsString();
        String repostId = JsonPath.read(body, "$.id");

        mockMvc.perform(get("/api/v1/reposts/by-post").param("postId", created.id().toString()).with(asUser(author.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].userId", hasItem(user.id().toString())));
        mockMvc.perform(get("/api/v1/reposts/by-user").param("userId", user.id().toString()).with(asUser(author.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].postId", hasItem(created.id().toString())));
        mockMvc.perform(get("/api/v1/reposts/{id}", repostId).with(asUser(author.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(repostId));

        mockMvc.perform(delete("/api/v1/reposts/{id}", repostId).with(asUser(other.id())))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/v1/reposts/{id}", repostId).with(asUser(user.id())))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/reposts/{id}", repostId).with(asUser(author.id())))
                .andExpect(status().isNotFound());
    }

    @Test
    void hiddenPostCannotBeReposted() throws Exception {
        UserReadDto author = createUser("author");
        UserReadDto user = createUser("reader");
        PostReadDto created = postService.create(new PostCreateDto("post", PostCategory.GENERAL, null), author.id());
        Post post = postRepository.findById(created.id()).orElseThrow();
        post.setStatus(PostStatus.HIDDEN);
        postRepository.saveAndFlush(post);

        mockMvc.perform(post("/api/v1/reposts")
                        .param("postId", created.id().toString())
                        .with(asUser(user.id())))
                .andExpect(status().isNotFound());
    }
}
