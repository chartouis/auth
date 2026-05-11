package yzarr.auth.pipeline.stages;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yzarr.auth.model.RefreshToken;
import yzarr.auth.model.enums.RevokeReason;
import yzarr.auth.model.enums.TokenType;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.service.CookieService;
import yzarr.auth.service.TokenService;

@Component
@Slf4j
@RequiredArgsConstructor
public class LogoutStage implements AuthStage {
    private final TokenService tokenService;
    private final CookieService cookieService;

    @Override
    public AuthContext process(AuthContext context) {
        RefreshToken rtoken = tokenService.findValidRefreshToken(cookieService.getRefreshToken(context.getRequest()));
        rtoken.revoke(RevokeReason.LOGOUT);
        tokenService.save(rtoken);
        cookieService.clearCookie(TokenType.ACCESS_TOKEN, context.getResponse());
        cookieService.clearCookie(TokenType.REFRESH_TOKEN, context.getResponse());
        log.info("User logged out: userId={}", rtoken.getUser().getId());
        return context;
    }

}
