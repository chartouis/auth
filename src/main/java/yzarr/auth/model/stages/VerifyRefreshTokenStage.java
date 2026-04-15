package yzarr.auth.model.stages;

import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import yzarr.auth.config.CookieService;
import yzarr.auth.config.RefreshTokenService;
import yzarr.auth.model.AuthContext;
import yzarr.auth.model.AuthStage;
import yzarr.auth.model.User;

@Component
@Slf4j
public class VerifyRefreshTokenStage implements AuthStage {
    private final RefreshTokenService refreshTokenService;
    private final CookieService cookieService;

    public VerifyRefreshTokenStage(RefreshTokenService refreshTokenService, CookieService cookieService) {
        this.refreshTokenService = refreshTokenService;
        this.cookieService = cookieService;
    }

    @Override
    public AuthContext process(AuthContext context) {
        String token = cookieService.getRefreshToken(context.getRequest());
        if (token == null) {
            return context.stop();
        }
        Optional<User> user = refreshTokenService.getUserByToken(token);
        if (user.isEmpty()) {
            return context.stop();
        }
        context.setUser(user.get());
        return context;
    }

}
