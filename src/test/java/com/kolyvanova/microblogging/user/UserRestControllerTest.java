package com.kolyvanova.microblogging.user;

import com.kolyvanova.microblogging.support.AbstractControllerTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserRestControllerTest extends AbstractControllerTest {

    @Test
    void currentProfileContainsEmailAndPublicProfileDoesNot() throws Exception {
        UserReadDto user = createUser("ivan_ivanov");

        mockMvc.perform(get("/api/v1/users/me").with(asUser(user.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ivan_ivanov@mail.ru"));

        mockMvc.perform(get("/api/v1/users/{id}", user.id()).with(asUser(user.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.username").value("ivan_ivanov"));
    }
}
