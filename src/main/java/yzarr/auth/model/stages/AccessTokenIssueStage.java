package yzarr.auth.model.stages;

import java.util.UUID;

import yzarr.auth.config.CookieService;
import yzarr.auth.config.JwtService;
import yzarr.auth.model.AuthContext;
import yzarr.auth.model.AuthStage;

public class AccessTokenIssueStage implements AuthStage {
    private final CookieService cookieService;
    private final JwtService jwtService;

    public AccessTokenIssueStage(CookieService cookieService, JwtService jwtService) {
        this.cookieService = cookieService;
        this.jwtService = jwtService;
    }

    @Override
    public AuthContext process(AuthContext context) {
        UUID userId = context.getUser().getId();
        String token = jwtService.generateToken(userId.toString());
        cookieService.setAccessTokenCookie(token, context.getResponse());
        return context;
    }

}
