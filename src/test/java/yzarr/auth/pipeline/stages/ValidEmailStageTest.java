package yzarr.auth.pipeline.stages;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import yzarr.auth.model.AuthException;
import yzarr.auth.model.enums.ErrorCode;

class ValidEmailStageTest {

    @Test
    void validate_happyPath() {
        ValidEmailStage stage = new ValidEmailStage();
        stage.process(yzarr.auth.pipeline.AuthContext.builder().email("a@b.com").build());
    }

    @Test
    void validate_rejectsNull() {
        ValidEmailStage stage = new ValidEmailStage();
        assertThatThrownBy(() -> stage.process(yzarr.auth.pipeline.AuthContext.builder().email(null).build()))
                .isInstanceOf(AuthException.class)
                .extracting(e -> ((AuthException) e).getCode())
                .isEqualTo(ErrorCode.INVALID_EMAIL_FORMAT);
    }

    @Test
    void validate_rejectsInvalidFormat() {
        ValidEmailStage stage = new ValidEmailStage();
        assertThatThrownBy(() -> stage.process(yzarr.auth.pipeline.AuthContext.builder().email("not-an-email").build()))
                .isInstanceOf(AuthException.class)
                .extracting(e -> ((AuthException) e).getCode())
                .isEqualTo(ErrorCode.INVALID_EMAIL_FORMAT);
    }
}

