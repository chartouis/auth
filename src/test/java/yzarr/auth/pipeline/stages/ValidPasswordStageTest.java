package yzarr.auth.pipeline.stages;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import yzarr.auth.model.AuthException;
import yzarr.auth.model.enums.ErrorCode;

class ValidPasswordStageTest {

    @Test
    void validatePassword_happyPath() {
        ValidPasswordStage.validatePassword("Password123!", 8);
    }

    @Test
    void validatePassword_rejectsNull() {
        assertThatThrownBy(() -> ValidPasswordStage.validatePassword(null, 8))
                .isInstanceOf(AuthException.class)
                .extracting(e -> ((AuthException) e).getCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void validatePassword_rejectsTooShort() {
        assertThatThrownBy(() -> ValidPasswordStage.validatePassword("short", 8))
                .isInstanceOf(AuthException.class)
                .extracting(e -> ((AuthException) e).getCode())
                .isEqualTo(ErrorCode.PASSWORD_IS_TOO_SHORT);
    }

    @Test
    void validatePassword_rejectsWhitespace() {
        assertThatThrownBy(() -> ValidPasswordStage.validatePassword("abc defghi", 8))
                .isInstanceOf(AuthException.class)
                .extracting(e -> ((AuthException) e).getCode())
                .isEqualTo(ErrorCode.INVALID_CHARACTERS);
    }

    @Test
    void validatePassword_rejectsControlChars() {
        assertThatThrownBy(() -> ValidPasswordStage.validatePassword("abc\u0000defghi", 8))
                .isInstanceOf(AuthException.class)
                .extracting(e -> ((AuthException) e).getCode())
                .isEqualTo(ErrorCode.INVALID_CHARACTERS);
    }
}

