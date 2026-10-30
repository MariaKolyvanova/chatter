package com.kolyvanova.chatter.user;

import com.kolyvanova.chatter.support.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

    @Test
    void currentUserCanChangePassword() throws Exception {
        UserReadDto user = createUser("ivan_ivanov");

        mockMvc.perform(put("/api/v1/users/me/password")
                        .with(asUser(user.id()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword": "pass123456", "newPassword": "newpass123"}
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void changePasswordRejectsWrongCurrentPassword() throws Exception {
        UserReadDto user = createUser("ivan_ivanov");

        mockMvc.perform(put("/api/v1/users/me/password")
                        .with(asUser(user.id()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword": "wrong-password", "newPassword": "newpass123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));
    }

    @Test
    void blockIsForbiddenForARegularUser() throws Exception {
        UserReadDto moderator = createUser("moderator");
        UserReadDto user = createUser("ivan_ivanov");

        mockMvc.perform(put("/api/v1/users/{id}/block", user.id()).with(asUser(moderator.id())))
                .andExpect(status().isForbidden());
    }

    @Test
    void moderatorCanBlockAndUnblockAUser() throws Exception {
        UserReadDto moderator = createUser("moderator");
        UserReadDto user = createUser("ivan_ivanov");

        mockMvc.perform(put("/api/v1/users/{id}/block", user.id())
                        .with(jwt().jwt(token -> token.subject(moderator.id().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_MODERATOR"))))
                .andExpect(status().isNoContent());

        mockMvc.perform(put("/api/v1/users/{id}/unblock", user.id())
                        .with(jwt().jwt(token -> token.subject(moderator.id().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_MODERATOR"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void moderatorCannotChangeRoles() throws Exception {
        UserReadDto moderator = createUser("moderator");
        UserReadDto user = createUser("ivan_ivanov");

        mockMvc.perform(put("/api/v1/users/{id}/role", user.id())
                        .param("role", "MODERATOR")
                        .with(jwt().jwt(token -> token.subject(moderator.id().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_MODERATOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminGrantsAndRevokesRoles() throws Exception {
        UserReadDto admin = createUser("admin_user");
        UserReadDto user = createUser("ivan_ivanov");

        mockMvc.perform(put("/api/v1/users/{id}/role", user.id())
                        .param("role", "ADMIN")
                        .with(jwt().jwt(token -> token.subject(admin.id().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());

        mockMvc.perform(put("/api/v1/users/{id}/role", user.id())
                        .param("role", "USER")
                        .with(jwt().jwt(token -> token.subject(admin.id().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void adminCannotChangeOwnRole() throws Exception {
        UserReadDto admin = createUser("admin_user");

        mockMvc.perform(put("/api/v1/users/{id}/role", admin.id())
                        .param("role", "USER")
                        .with(jwt().jwt(token -> token.subject(admin.id().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void newUserReceivesTheDefaultAvatar() throws Exception {
        UserReadDto user = createUser("default_avatar");
        byte[] png = new ClassPathResource("avatar/default.png").getContentAsByteArray();

        mockMvc.perform(get("/api/v1/users/me").with(asUser(user.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarKey").value("avatar/default.png"));

        mockMvc.perform(get("/api/v1/users/{id}/avatar", user.id()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(png));
    }

    @Test
    void currentUserCanReplaceAvatarAndAnyoneCanReadIt() throws Exception {
        UserReadDto user = createUser("avatar_user");
        byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
        MockMultipartFile image = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", jpeg);

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/users/me/avatar")
                        .file(image)
                        .with(asUser(user.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarKey").exists());

        mockMvc.perform(get("/api/v1/users/{id}/avatar", user.id()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"))
                .andExpect(content().bytes(jpeg));
    }

    @Test
    void avatarRejectsAFileThatIsNotAnImage() throws Exception {
        UserReadDto user = createUser("avatar_user");
        MockMultipartFile text = new MockMultipartFile("file", "note.txt", "text/plain", "hello".getBytes());

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/users/me/avatar")
                        .file(text)
                        .with(asUser(user.id())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported file type: text/plain"));
    }
}
