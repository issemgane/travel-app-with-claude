package com.wanderlust.api.auth;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-jwt-secret-key-that-is-long-enough-for-hs256-algorithm-minimum-256-bits";
    private static final long EXPIRATION_MS = 3600000L; // 1 hour

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION_MS);
    }

    @Test
    void generateToken_producesNonNullToken() {
        UUID userId = UUID.randomUUID();

        String token = jwtService.generateToken(userId, "traveler");

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void getUserId_extractsCorrectUUID() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "traveler");

        UUID extracted = jwtService.getUserId(token);

        assertThat(extracted).isEqualTo(userId);
    }

    @Test
    void isValid_returnsTrueForValidToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "traveler");

        assertThat(jwtService.isValid(token)).isTrue();
    }

    @Test
    void isValid_returnsFalseForGarbageToken() {
        assertThat(jwtService.isValid("this.is.garbage")).isFalse();
    }

    @Test
    void isValid_returnsFalseForExpiredToken() throws InterruptedException {
        JwtService shortLived = new JwtService(SECRET, 1L);
        String token = shortLived.generateToken(UUID.randomUUID(), "traveler");

        Thread.sleep(50);

        assertThat(shortLived.isValid(token)).isFalse();
    }

    @Test
    void parseToken_extractsCorrectUsernameClaim() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "globetrotter");

        Claims claims = jwtService.parseToken(token);

        assertThat(claims.get("username", String.class)).isEqualTo("globetrotter");
    }

    @Test
    void constructor_usesDefaultSecretWhenBlankStringProvided() {
        JwtService defaultService = new JwtService("", EXPIRATION_MS);
        UUID userId = UUID.randomUUID();

        String token = defaultService.generateToken(userId, "traveler");

        assertThat(token).isNotNull().isNotBlank();
        assertThat(defaultService.isValid(token)).isTrue();
        assertThat(defaultService.getUserId(token)).isEqualTo(userId);
    }
}
