package com.kolyvanova.chatter.auth;

import com.jayway.jsonpath.JsonPath;
import com.kolyvanova.chatter.security.JwtProperties;
import com.kolyvanova.chatter.support.AbstractControllerTest;
import com.kolyvanova.chatter.user.UserReadDto;
import com.kolyvanova.chatter.user.UserService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthRestControllerTest extends AbstractControllerTest {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private JwtProperties jwtProperties;

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

    @Test
    void loginRejectsBlockedUser() throws Exception {
        UserReadDto user = createUser("ivan_ivanov");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "moderator", "n/a", List.of(new SimpleGrantedAuthority("ROLE_MODERATOR"))));
        try {
            userService.block(user.id());
        } finally {
            SecurityContextHolder.clearContext();
        }

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "ivan_ivanov", "password": "pass123456"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginAcceptsAUsernameInAnyCase() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "Ivan_Ivanov",
                                  "email": "ivan.case@mail.ru",
                                  "password": "pass123456",
                                  "displayName": "Ivan Ivanov",
                                  "birthDate": "2000-01-01"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("ivan_ivanov"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "IVAN_IVANOV", "password": "pass123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(blankOrNullString())));
    }

    @Test
    void expiredAndForgedTokensAreRejected() throws Exception {
        UserReadDto user = createUser("ivan_ivanov");
        String body = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "ivan_ivanov", "password": "pass123456"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(body, "$.accessToken");

        mockMvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ivan_ivanov"));

        String expired = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder()
                        .issuer(jwtProperties.issuer())
                        .subject(user.id().toString())
                        .issuedAt(Instant.now().minusSeconds(600))
                        .expiresAt(Instant.now().minusSeconds(300))
                        .claim("role", "ROLE_USER")
                        .build())).getTokenValue();
        mockMvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + expired))
                .andExpect(status().isUnauthorized());

        SecretKey otherKey = new SecretKeySpec(new byte[32], "HmacSHA256");
        String forged = new NimbusJwtEncoder(new ImmutableSecret<>(otherKey)).encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder()
                        .issuer(jwtProperties.issuer())
                        .subject(user.id().toString())
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(jwtProperties.accessSeconds()))
                        .claim("role", "ROLE_USER")
                        .build())).getTokenValue();
        mockMvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + forged))
                .andExpect(status().isUnauthorized());
    }
}
