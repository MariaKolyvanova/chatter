package com.kolyvanova.chatter.topicSubscriptions;

import com.kolyvanova.chatter.support.AbstractControllerTest;
import com.kolyvanova.chatter.user.UserReadDto;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TopicSubscriptionsRestControllerTest extends AbstractControllerTest {

    @Test
    void subscribeRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/topicsSub").param("postCategory", "SCIENCE"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required"));
    }

    @Test
    void subscribeListAndUnsubscribe() throws Exception {
        UserReadDto user = createUser("reader");

        mockMvc.perform(post("/api/v1/topicsSub")
                        .param("postCategory", "SCIENCE")
                        .with(asUser(user.id())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(user.id().toString()))
                .andExpect(jsonPath("$.category").value("SCIENCE"));

        mockMvc.perform(get("/api/v1/topicsSub/me").with(asUser(user.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category").value("SCIENCE"));

        mockMvc.perform(delete("/api/v1/topicsSub")
                        .param("postCategory", "SCIENCE")
                        .with(asUser(user.id())))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/topicsSub/me").with(asUser(user.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void duplicateSubscriptionIsConflict() throws Exception {
        UserReadDto user = createUser("reader");

        mockMvc.perform(post("/api/v1/topicsSub")
                        .param("postCategory", "SCIENCE")
                        .with(asUser(user.id())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/topicsSub")
                        .param("postCategory", "SCIENCE")
                        .with(asUser(user.id())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Topic subscription already exists"));
    }

    @Test
    void unsubscribeMissingTopicIsNotFound() throws Exception {
        UserReadDto user = createUser("reader");

        mockMvc.perform(delete("/api/v1/topicsSub")
                        .param("postCategory", "SCIENCE")
                        .with(asUser(user.id())))
                .andExpect(status().isNotFound());
    }

    @Test
    void subscribeFailsWhenUserDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/v1/topicsSub")
                        .param("postCategory", "SCIENCE")
                        .with(asUser(UUID.randomUUID())))
                .andExpect(status().isNotFound());
    }
}
