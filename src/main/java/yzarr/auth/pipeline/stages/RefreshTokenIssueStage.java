package yzarr.auth.pipeline.stages;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.service.CookieService;
import yzarr.auth.service.TokenService;

/* Requires User in the Context. Goes after AuthenticationStage
   Puts Refresh token in response cookies */
@Component
@RequiredArgsConstructor
public class RefreshTokenIssueStage implements AuthStage {

    private final TokenService tokenService;
    private final CookieService cookieService;

    @Override
    public AuthContext process(AuthContext context) {
        String token = tokenService.generateRefreshToken(context.getUser(), context.isRememberMe());
        cookieService.setRefreshTokenCookie(token, context.getResponse(), context.isRememberMe());
        return context;
    }

}
