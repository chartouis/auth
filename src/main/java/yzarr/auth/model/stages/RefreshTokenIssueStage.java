package yzarr.auth.model.stages;

import org.springframework.stereotype.Component;

import yzarr.auth.config.CookieService;
import yzarr.auth.config.RefreshTokenService;
import yzarr.auth.model.AuthContext;
import yzarr.auth.model.AuthStage;

/* Requires User in the Context. Goes after AuthenticationStage
   Puts Refresh token in response cookies */
@Component
public class RefreshTokenIssueStage implements AuthStage {

    private final RefreshTokenService tokenService;
    private final CookieService cookieService;

    public RefreshTokenIssueStage(RefreshTokenService tokenService, CookieService cookieService) {
        this.tokenService = tokenService;
        this.cookieService = cookieService;
    }

    @Override
    public AuthContext process(AuthContext context) {
        String token = tokenService.generate(context.getUser(), context.isRememberMe());
        cookieService.setRefreshTokenCookie(token, context.getResponse(), context.isRememberMe());
        return context;
    }

}
