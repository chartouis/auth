package yzarr.auth.pipeline.stages;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import yzarr.auth.model.RefreshToken;
import yzarr.auth.model.enums.RevokeReason;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.service.CookieService;
import yzarr.auth.service.TokenService;

@Component
@RequiredArgsConstructor
public class RefreshTokenRotationStage implements AuthStage {
    private final TokenService tokenService;
    private final CookieService cookieService;

    @Override
    public AuthContext process(AuthContext context) {
        RefreshToken token = tokenService.findValidRefreshToken(context.getToken());
        token.revoke(RevokeReason.ROTATED);
        String newTokenString = tokenService.generateRefreshToken(token.getUser(), token.isRememberMe());
        RefreshToken newToken = tokenService.findValidRefreshToken(newTokenString);
        newToken.setAbsoluteExpiry(token.getAbsoluteExpiry());
        tokenService.save(newToken);
        tokenService.save(token);
        cookieService.setRefreshTokenCookie(newTokenString, context.getResponse(), newToken.isRememberMe());
        return context;
    }

}
