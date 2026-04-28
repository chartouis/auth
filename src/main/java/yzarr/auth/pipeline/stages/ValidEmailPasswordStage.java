package yzarr.auth.pipeline.stages;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import yzarr.auth.model.AuthException;
import yzarr.auth.model.enums.ErrorCode;
import yzarr.auth.pipeline.AuthContext;

@Component
@Slf4j
public class ValidEmailPasswordStage implements AuthStage {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$");

    @Override
    public AuthContext process(AuthContext context) {
        validateEmail(context.getEmail());
        validatePassword(context.getPassword(), context.getProps().getMinPasswordLength());
        return context;
    }

    private void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new AuthException(ErrorCode.INVALID_EMAIL_FORMAT);
        }
    }

    private void validatePassword(String password, int minLength) {
        if (password == null) {
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (password.length() < minLength) {
            throw new AuthException(ErrorCode.PASSWORD_IS_TOO_SHORT);
        }

        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);

            if (Character.isISOControl(c) || Character.isWhitespace(c)) {
                throw new AuthException(ErrorCode.INVALID_CHARACTERS);
            }
        }
    }
}