package yzarr.auth.pipeline.stages;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import yzarr.auth.model.AuthException;
import yzarr.auth.model.enums.ErrorCode;
import yzarr.auth.pipeline.AuthContext;

@Component
public class ValidEmailStage implements AuthStage {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$");

    @Override
    public AuthContext process(AuthContext context) {
        validateEmail(context.getEmail());
        return context;
    }

    private void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new AuthException(ErrorCode.INVALID_EMAIL_FORMAT);
        }
    }

}
