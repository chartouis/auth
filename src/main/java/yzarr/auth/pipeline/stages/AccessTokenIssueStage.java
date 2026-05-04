package yzarr.auth.pipeline.stages;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.service.CookieService;
import yzarr.auth.service.JwtService;

@Component
@Slf4j
@RequiredArgsConstructor
public class AccessTokenIssueStage implements AuthStage {
    private final CookieService cookieService;
    private final JwtService jwtService;

    @Override
    public AuthContext process(AuthContext context) {
        UUID userId = context.getUser().getId();
        String token = jwtService.generateToken(userId.toString());
        cookieService.setAccessTokenCookie(token, context.getResponse());
        return context;
    }

}
