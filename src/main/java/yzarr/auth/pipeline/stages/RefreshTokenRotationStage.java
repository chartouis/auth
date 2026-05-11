package yzarr.auth.pipeline.stages;

import java.time.Instant;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yzarr.auth.model.AuthException;
import yzarr.auth.model.RefreshToken;
import yzarr.auth.model.enums.ErrorCode;
import yzarr.auth.model.enums.RevokeReason;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.service.CookieService;
import yzarr.auth.service.TokenService;

@Component
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenRotationStage implements AuthStage {
    private final TokenService tokenService;
    private final CookieService cookieService;

    @Override
    public AuthContext process(AuthContext context) {
        RefreshToken token = tokenService.findValidRefreshToken(context.getToken());
        if (token.getIssuedAt()
                .isAfter(Instant.now().minusMillis(context.getProps().getRefreshCooldownMs()))) {
            log.warn("Refresh token rotation cooldown triggered: userId={}", token.getUser().getId());
            throw new AuthException(ErrorCode.TOKEN_ROTATION_COOLDOWN);
        }
        log.debug("Rotating refresh token: userId={}", token.getUser().getId());
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
