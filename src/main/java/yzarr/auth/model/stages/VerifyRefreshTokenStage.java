package yzarr.auth.model.stages;

import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import yzarr.auth.config.CookieService;
import yzarr.auth.config.TokenService;
import yzarr.auth.model.AuthContext;
import yzarr.auth.model.AuthStage;
import yzarr.auth.model.TokenType;
import yzarr.auth.model.User;

@Component
@Slf4j
public class VerifyRefreshTokenStage implements AuthStage {
    private final TokenService tokenService;
    private final CookieService cookieService;

    public VerifyRefreshTokenStage(TokenService tokenService, CookieService cookieService) {
        this.tokenService = tokenService;
        this.cookieService = cookieService;
    }

    @Override
    public AuthContext process(AuthContext context) {
        String token = cookieService.getRefreshToken(context.getRequest());
        if (token == null) {
            return context.stop();
        }
        Optional<User> user = tokenService.getUserByToken(token, TokenType.REFRESH_TOKEN);
        if (user.isEmpty()) {
            return context.stop();
        }
        context.setUser(user.get());
        return context;
    }

}
