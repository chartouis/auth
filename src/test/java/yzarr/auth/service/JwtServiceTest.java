package yzarr.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import yzarr.auth.AuthProperties;
import yzarr.auth.model.TokenException;
import yzarr.auth.model.enums.TokenFailureReason;

class JwtServiceTest {

    private static AuthProperties propsWithSecretAndExpiry(long accessTokenExpiryMs) {
        AuthProperties props = new AuthProperties();
        // 256-bit key, base64
        props.setJwtSecret("dGVzdHNlY3JldGtleXRlc3RzZWNyZXRrZXl0ZXN0c2VjcmV0a2V5dGVzdA==");
        props.setAccessTokenExpiryMs(accessTokenExpiryMs);
        return props;
    }

    @Test
    void generateToken_returnsNonNullString() {
        JwtService jwtService = new JwtService(propsWithSecretAndExpiry(60_000));
        String token = jwtService.generateToken("user-123");
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void extractSubject_returnsSubjectPassedToGenerateToken() {
        JwtService jwtService = new JwtService(propsWithSecretAndExpiry(60_000));
        String token = jwtService.generateToken("user-123");
        assertThat(jwtService.extractSubject(token)).isEqualTo("user-123");
    }

    @Test
    void validateToken_doesNotThrow_whenValidAndSubjectMatches() {
        JwtService jwtService = new JwtService(propsWithSecretAndExpiry(60_000));
        String token = jwtService.generateToken("user-123");
        jwtService.validateToken(token, "user-123");
    }

    @Test
    void validateToken_throwsExpired_whenExpiredToken() throws Exception {
        AuthProperties props = propsWithSecretAndExpiry(0);
        JwtService jwtService = new JwtService(props);

        // ensure any time-based boundary is crossed
        Thread.sleep(2);

        String token = jwtService.generateToken("user-123");

        assertThatThrownBy(() -> jwtService.validateToken(token, "user-123"))
                .isInstanceOf(TokenException.class)
                .extracting(e -> ((TokenException) e).getReason())
                .isEqualTo(TokenFailureReason.EXPIRED);
    }

    @Test
    void validateToken_throwsInvalid_whenSubjectDoesNotMatch() {
        JwtService jwtService = new JwtService(propsWithSecretAndExpiry(60_000));
        String token = jwtService.generateToken("user-123");

        assertThatThrownBy(() -> jwtService.validateToken(token, "user-999"))
                .isInstanceOf(TokenException.class)
                .extracting(e -> ((TokenException) e).getReason())
                .isEqualTo(TokenFailureReason.INVALID);
    }

    @Test
    void validateToken_throwsInvalid_onGarbageToken() {
        JwtService jwtService = new JwtService(propsWithSecretAndExpiry(60_000));

        assertThatThrownBy(() -> jwtService.validateToken("not-a-jwt", "user-123"))
                .isInstanceOf(TokenException.class)
                .extracting(e -> ((TokenException) e).getReason())
                .isEqualTo(TokenFailureReason.INVALID);
    }

    @Test
    void validateToken_expired_viaReflection_overrideExpiry() throws Exception {
        AuthProperties props = propsWithSecretAndExpiry(60_000);
        JwtService jwtService = new JwtService(props);

        Field accessExpiryField = AuthProperties.class.getDeclaredField("accessTokenExpiryMs");
        accessExpiryField.setAccessible(true);
        accessExpiryField.set(props, 0L);

        Thread.sleep(2);
        String token = jwtService.generateToken("user-123");

        assertThatThrownBy(() -> jwtService.validateToken(token, "user-123"))
                .isInstanceOf(TokenException.class)
                .extracting(e -> ((TokenException) e).getReason())
                .isEqualTo(TokenFailureReason.EXPIRED);
    }
}

