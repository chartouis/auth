package yzarr.auth.pipeline.stages;

import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import yzarr.auth.model.AuthException;
import yzarr.auth.model.User;
import yzarr.auth.model.enums.ErrorCode;
import yzarr.auth.model.enums.TokenType;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.service.CookieService;
import yzarr.auth.service.TokenService;

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
            throw new AuthException(ErrorCode.NO_REFRESH_TOKEN);
        }
        Optional<User> user = tokenService.getUserByToken(token, TokenType.REFRESH_TOKEN);
        if (user.isEmpty()) {
            throw new AuthException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        context.setUser(user.get());
        return context;
    }

}
