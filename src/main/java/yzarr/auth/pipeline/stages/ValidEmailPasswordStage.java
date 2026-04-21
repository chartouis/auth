package yzarr.auth.pipeline.stages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import yzarr.auth.model.AuthException;
import yzarr.auth.model.enums.ErrorCode;
import yzarr.auth.pipeline.AuthContext;

/* Checks email and password for null, then checks password length and validates email wtih OWASP regex */
@Component
@Slf4j
public class ValidEmailPasswordStage implements AuthStage {
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$";
    private static final Pattern PATTERN = Pattern.compile(EMAIL_REGEX);

    @Override
    public AuthContext process(AuthContext context) {
        if (!isValidEmail(context.getEmail())) {
            context.stop();
            throw new AuthException(ErrorCode.INVALID_EMAIL_FORMAT);
        }
        if (context.getPassword().length() < context.getProps().getMinPasswordLength()) {
            context.stop();
            throw new AuthException(ErrorCode.PASSWORD_IS_TOO_SHORT);
        }
        if (!isValidPassword(context.getPassword())) {
            context.stop();
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }
        return context;
    }

    public static boolean isValidEmail(String email) {
        if (email == null)
            return false;
        Matcher matcher = PATTERN.matcher(email);
        return matcher.matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null)
            return false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);

            if (Character.isISOControl(c))
                return false;
            if (Character.isWhitespace(c))
                return false;
        }

        return true;
    }
}
