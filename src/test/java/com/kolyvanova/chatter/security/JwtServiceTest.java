package com.kolyvanova.chatter.security;

import com.kolyvanova.chatter.auth.SecurityUser;
import com.kolyvanova.chatter.user.User;
import com.kolyvanova.chatter.user.UserRole;
import com.kolyvanova.chatter.user.UserStatus;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtServiceTest {

    @Test
    void createPutsSubjectRoleAndExpiryIntoTheToken() {
        String secret = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
        JwtProperties properties = new JwtProperties(secret, "admin", 900);
        SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(secret), "HmacSHA256");
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        JwtService jwtService = new JwtService(encoder, properties);
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("ivan")
                .email("ivan@mail.ru")
                .passwordHash("hash")
                .displayName("Ivan")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        Jwt jwt = NimbusJwtDecoder.withSecretKey(key).build().decode(jwtService.create(new SecurityUser(user)));

        assertEquals(user.getId().toString(), jwt.getSubject());
        assertEquals("ROLE_USER", jwt.getClaimAsString("role"));
        assertEquals("admin", jwt.getClaimAsString("iss"));
        assertEquals(Duration.ofSeconds(900), Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()));
    }
}
