package com.kolyvanova.microblogging.like;

import com.kolyvanova.microblogging.post.PostCategory;
import com.kolyvanova.microblogging.post.PostCreateDto;
import com.kolyvanova.microblogging.post.PostReadDto;
import com.kolyvanova.microblogging.post.PostService;
import com.kolyvanova.microblogging.support.AbstractControllerTest;
import com.kolyvanova.microblogging.user.UserReadDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LikeRestControllerTest extends AbstractControllerTest {

    @Autowired
    private PostService postService;

    @Test
    void createLikeByPostId() throws Exception {
        UserReadDto author = createUser("author");
        UserReadDto user = createUser("reader");
        PostReadDto created = postService.create(new PostCreateDto("post", PostCategory.GENERAL, null), author.id());

        mockMvc.perform(post("/api/v1/likes")
                        .param("postId", created.id().toString())
                        .with(asUser(user.id())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.postId").value(created.id().toString()))
                .andExpect(jsonPath("$.userId").value(user.id().toString()));
    }
}
