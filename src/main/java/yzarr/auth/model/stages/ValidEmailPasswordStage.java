package yzarr.auth.model.stages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import yzarr.auth.model.AuthContext;
import yzarr.auth.model.AuthStage;

/* Checks email and password for null, then checks password length and validates email wtih OWASP regex */
@Component
@Slf4j
public class ValidEmailPasswordStage implements AuthStage {
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$";
    private static final Pattern PATTERN = Pattern.compile(EMAIL_REGEX);

    @Override
    public AuthContext process(AuthContext context) {
        if (!isValidEmail(context.getEmail())
                || !isValidPassword(context.getPassword(), context.getProps().getMinPasswordLength())) {

            return context.stop();
        }
        return context;
    }

    public static boolean isValidEmail(String email) {
        if (email == null)
            return false;
        Matcher matcher = PATTERN.matcher(email);
        return matcher.matches();
    }

    public static boolean isValidPassword(String password, short length) {
        if (password == null)
            return false;
        if (password.length() < length)
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
