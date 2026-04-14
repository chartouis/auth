package yzarr.auth.model.stages;

import java.util.Optional;

import yzarr.auth.config.CookieService;
import yzarr.auth.config.RefreshTokenService;
import yzarr.auth.model.AuthContext;
import yzarr.auth.model.AuthStage;
import yzarr.auth.model.User;

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
            context.stop();
        }
        Optional<User> user = refreshTokenService.getUserByToken(token);
        if (user.isEmpty()) {
            context.stop();
        }
        context.setUser(user.get());
        return context;
    }

}
