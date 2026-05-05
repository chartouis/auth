package yzarr.auth.pipeline.stages;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import yzarr.auth.model.AuthException;
import yzarr.auth.model.enums.ErrorCode;
import yzarr.auth.pipeline.AuthContext;

@Component
@Slf4j
public class ValidPasswordStage implements AuthStage {

    @Override
    public AuthContext process(AuthContext context) {
        validatePassword(context.getPassword(), context.getProps().getMinPasswordLength());
        return context;
    }

    public static void validatePassword(String password, int minLength) {
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