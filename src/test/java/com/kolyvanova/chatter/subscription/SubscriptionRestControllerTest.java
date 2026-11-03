package com.kolyvanova.chatter.subscription;

import com.jayway.jsonpath.JsonPath;
import com.kolyvanova.chatter.support.AbstractControllerTest;
import com.kolyvanova.chatter.user.UserReadDto;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SubscriptionRestControllerTest extends AbstractControllerTest {

    @Test
    void subscribesListsAndUnsubscribes() throws Exception {
        UserReadDto subscriber = createUser("subscriber");
        UserReadDto author = createUser("author");

        String body = mockMvc.perform(post("/api/v1/subscriptions")
                        .param("authorId", author.id().toString())
                        .with(asUser(subscriber.id())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subscriberId").value(subscriber.id().toString()))
                .andExpect(jsonPath("$.authorId").value(author.id().toString()))
                .andReturn().getResponse().getContentAsString();
        String subscriptionId = JsonPath.read(body, "$.id");

        mockMvc.perform(post("/api/v1/subscriptions")
                        .param("authorId", author.id().toString())
                        .with(asUser(subscriber.id())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Subscription already exists"));

        mockMvc.perform(get("/api/v1/subscriptions/{id}", subscriptionId).with(asUser(subscriber.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(subscriptionId));
        mockMvc.perform(get("/api/v1/subscriptions/by-subscriber")
                        .param("subscriberId", subscriber.id().toString())
                        .with(asUser(author.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].authorId", hasItem(author.id().toString())));
        mockMvc.perform(get("/api/v1/subscriptions/by-author")
                        .param("authorId", author.id().toString())
                        .with(asUser(subscriber.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].subscriberId", hasItem(subscriber.id().toString())));

        mockMvc.perform(delete("/api/v1/subscriptions")
                        .param("authorId", author.id().toString())
                        .with(asUser(subscriber.id())))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/subscriptions/{id}", subscriptionId).with(asUser(subscriber.id())))
                .andExpect(status().isNotFound());
    }

    @Test
    void cannotSubscribeToSelf() throws Exception {
        UserReadDto user = createUser("subscriber");

        mockMvc.perform(post("/api/v1/subscriptions")
                        .param("authorId", user.id().toString())
                        .with(asUser(user.id())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A user cannot subscribe to themselves"));
    }
}
