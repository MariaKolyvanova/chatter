package com.kolyvanova.chatter.user;

import com.kolyvanova.chatter.image.MediaProperties;
import com.kolyvanova.chatter.post.PostCategory;
import com.kolyvanova.chatter.post.PostCreateDto;
import com.kolyvanova.chatter.post.PostReadDto;
import com.kolyvanova.chatter.post.PostRepository;
import com.kolyvanova.chatter.post.PostService;
import com.kolyvanova.chatter.support.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserRestControllerTest extends AbstractControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MediaProperties mediaProperties;

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
    void moderatorListsBlockedUsersAndReadsStatus() throws Exception {
        UserReadDto moderator = createUser("moderator");
        UserReadDto user = createUser("ivan_ivanov");
        User stored = userRepository.findById(moderator.id()).orElseThrow();
        stored.setRole(UserRole.MODERATOR);
        userRepository.saveAndFlush(stored);
        var asModerator = jwt().jwt(token -> token.subject(moderator.id().toString()))
                .authorities(new SimpleGrantedAuthority("ROLE_MODERATOR"));

        mockMvc.perform(get("/api/v1/users").param("status", "BLOCKED").with(asUser(user.id())))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/users/{id}/block", user.id()).with(asModerator))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/users").param("status", "BLOCKED").param("size", "100").with(asModerator))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].username", hasItem("ivan_ivanov")))
                .andExpect(jsonPath("$.content[?(@.username == 'ivan_ivanov')].role").doesNotExist());

        mockMvc.perform(get("/api/v1/users/{id}", user.id()).with(asModerator))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"))
                .andExpect(jsonPath("$.role").doesNotExist());

        mockMvc.perform(put("/api/v1/users/{id}/unblock", user.id()).with(asModerator))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/users").param("status", "BLOCKED").param("size", "100").with(asModerator))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].username", not(hasItem("ivan_ivanov"))));
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
    void newUserHasNoAvatar() throws Exception {
        UserReadDto user = createUser("no_avatar");

        mockMvc.perform(get("/api/v1/users/me").with(asUser(user.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarKey").value(nullValue()));

        mockMvc.perform(get("/api/v1/users/{id}/avatar", user.id()))
                .andExpect(status().isNotFound());
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

        mockMvc.perform(delete("/api/v1/users/me/avatar").with(asUser(user.id())))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/users/me").with(asUser(user.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarKey").value(nullValue()));
        mockMvc.perform(get("/api/v1/users/{id}/avatar", user.id()))
                .andExpect(status().isNotFound());
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

    @Test
    void avatarRejectsAnEmptyOrTooLargeFile() throws Exception {
        UserReadDto user = createUser("avatar_user");
        MockMultipartFile empty = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);
        MockMultipartFile large = new MockMultipartFile(
                "file", "large.jpg", "image/jpeg", new byte[(int) mediaProperties.maxBytes() + 1]);

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/users/me/avatar")
                        .file(empty)
                        .with(asUser(user.id())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("File is empty"));

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/users/me/avatar")
                        .file(large)
                        .with(asUser(user.id())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("File is too large"));
    }

    @Test
    void currentUserCanUpdateAndDeleteTheProfile() throws Exception {
        UserReadDto user = createUser("ivan_ivanov");

        mockMvc.perform(put("/api/v1/users/me")
                        .with(asUser(user.id()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "Ivan_New",
                                  "email": "new@mail.ru",
                                  "displayName": "Ivan New",
                                  "bio": "new bio",
                                  "location": "Kazan",
                                  "birthDate": "2001-02-02"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ivan_new"))
                .andExpect(jsonPath("$.email").value("new@mail.ru"))
                .andExpect(jsonPath("$.displayName").value("Ivan New"));

        mockMvc.perform(delete("/api/v1/users/me").with(asUser(user.id())))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/users/me").with(asUser(user.id())))
                .andExpect(status().isNotFound());
    }

    @Test
    void findsAUserByUsernameAndAnAdminAlsoSeesTheRole() throws Exception {
        UserReadDto user = createUser("Ivan_Name");
        UserReadDto admin = createUser("admin_user");
        User stored = userRepository.findById(admin.id()).orElseThrow();
        stored.setRole(UserRole.ADMIN);
        userRepository.saveAndFlush(stored);

        mockMvc.perform(get("/api/v1/users/by-username")
                        .param("username", "IVAN_NAME")
                        .with(asUser(user.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ivan_name"))
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.role").doesNotExist());

        mockMvc.perform(get("/api/v1/users/by-username")
                        .param("username", "ivan_name")
                        .with(asUser(admin.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ivan_name"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void adminListsUsersByRole() throws Exception {
        UserReadDto user = createUser("ivan_ivanov");
        UserReadDto admin = createUser("admin_user");
        User stored = userRepository.findById(admin.id()).orElseThrow();
        stored.setRole(UserRole.ADMIN);
        userRepository.saveAndFlush(stored);

        mockMvc.perform(get("/api/v1/users").param("role", "ADMIN").with(asUser(user.id())))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/users").param("role", "ADMIN").param("size", "100").with(asUser(admin.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].username", hasItem("admin_user")))
                .andExpect(jsonPath("$.content[?(@.username == 'admin_user')].role", hasItem("ADMIN")))
                .andExpect(jsonPath("$.content[*].username", not(hasItem("ivan_ivanov"))));
    }

    @Test
    void avatarAcceptsPngAndHeif() throws Exception {
        UserReadDto user = createUser("formats");
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
        byte[] heif = {0x00, 0x00, 0x00, 0x18, 'f', 't', 'y', 'p', 'm', 'i', 'f', '1'};

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/users/me/avatar")
                        .file(new MockMultipartFile("file", "avatar.png", "image/png", png))
                        .with(asUser(user.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarKey", endsWith(".png")));
        mockMvc.perform(get("/api/v1/users/{id}/avatar", user.id()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(png));

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/users/me/avatar")
                        .file(new MockMultipartFile("file", "avatar.heif", "image/heif", heif))
                        .with(asUser(user.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarKey", endsWith(".heif")));
        mockMvc.perform(get("/api/v1/users/{id}/avatar", user.id()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/heif"))
                .andExpect(content().bytes(heif));
    }

    @Test
    void deletingAUserRemovesTheAvatarAndPostImage() throws Exception {
        UserReadDto user = createUser("files");
        byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
        MockMultipartFile image = new MockMultipartFile("file", "photo.jpg", "image/jpeg", jpeg);

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/users/me/avatar")
                        .file(image)
                        .with(asUser(user.id())))
                .andExpect(status().isOk());
        PostReadDto post = postService.create(new PostCreateDto("post", PostCategory.GENERAL, null), user.id());
        mockMvc.perform(multipart("/api/v1/posts/{id}/image", post.id())
                        .file(image)
                        .with(asUser(user.id())))
                .andExpect(status().isOk());

        Path avatar = mediaProperties.directory().resolve(userRepository.findById(user.id()).orElseThrow().getAvatarKey());
        Path photo = mediaProperties.directory().resolve(postRepository.findById(post.id()).orElseThrow().getImageKey());
        assertTrue(Files.exists(avatar));
        assertTrue(Files.exists(photo));

        mockMvc.perform(delete("/api/v1/users/me").with(asUser(user.id())))
                .andExpect(status().isNoContent());

        assertFalse(Files.exists(avatar));
        assertFalse(Files.exists(photo));
        assertTrue(userRepository.findById(user.id()).isEmpty());
        assertTrue(postRepository.findById(post.id()).isEmpty());
    }
}
