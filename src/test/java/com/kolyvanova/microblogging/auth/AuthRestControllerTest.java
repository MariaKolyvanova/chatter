package com.kolyvanova.microblogging.auth;

import com.kolyvanova.microblogging.support.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthRestControllerTest extends AbstractControllerTest {

    @Test
    void registerReturnsCreatedUser() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ivan_ivanov",
                                  "email": "ivan@mail.ru",
                                  "password": "pass123456",
                                  "displayName": "Ivan Ivanov",
                                  "birthDate": "2000-01-01"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("ivan_ivanov"))
                .andExpect(jsonPath("$.email").value("ivan@mail.ru"));
    }

    @Test
    void loginReturnsAccessToken() throws Exception {
        createUser("ivan_ivanov");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "ivan_ivanov", "password": "pass123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(blankOrNullString())))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void loginRejectsWrongPassword() throws Exception {
        createUser("ivan_ivanov");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "ivan_ivanov", "password": "wrong-password"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
