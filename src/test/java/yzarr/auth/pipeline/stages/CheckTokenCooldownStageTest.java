package yzarr.auth.pipeline.stages;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import yzarr.auth.model.AuthException;
import yzarr.auth.model.VerificationToken;
import yzarr.auth.model.User;
import yzarr.auth.model.enums.ErrorCode;
import yzarr.auth.model.enums.TokenType;

class CheckTokenCooldownStageTest {

    @Test
    void isBeforeCooldown_throwsEmailAlreadySent_whenIssuedWithinCooldownWindow() {
        VerificationToken token = new VerificationToken("hash", new User("a@b.com", "hash"),
                Instant.now().plusSeconds(60), TokenType.EMAIL_VERIFICATION);
        token.setIssuedAt(Instant.now().minusSeconds(10));

        assertThatThrownBy(() -> CheckTokenCooldownStage.isBeforeCooldown(token, 60_000L))
                .isInstanceOf(AuthException.class)
                .extracting(e -> ((AuthException) e).getCode())
                .isEqualTo(ErrorCode.EMAIL_ALREADY_SENT);
    }

    @Test
    void isBeforeCooldown_doesNotThrow_whenIssuedBeforeCooldownWindow() {
        VerificationToken token = new VerificationToken("hash", new User("a@b.com", "hash"),
                Instant.now().plusSeconds(60), TokenType.EMAIL_VERIFICATION);
        token.setIssuedAt(Instant.now().minusSeconds(120));

        CheckTokenCooldownStage.isBeforeCooldown(token, 60_000L);
    }
}

