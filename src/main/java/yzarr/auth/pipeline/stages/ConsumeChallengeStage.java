package yzarr.auth.pipeline.stages;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import yzarr.auth.model.TokenException;
import yzarr.auth.model.VerificationToken;
import yzarr.auth.model.enums.VerificationTokenStatus;
import yzarr.auth.model.enums.TokenFailureReason;
import yzarr.auth.model.enums.TokenType;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.service.CookieService;
import yzarr.auth.service.TokenService;

@Component
@Slf4j
public class ConsumeChallengeStage implements AuthStage {
    private final CookieService cookieService;
    private final TokenService tokenService;

    public ConsumeChallengeStage(CookieService cookieService, TokenService tokenService) {
        this.cookieService = cookieService;
        this.tokenService = tokenService;
    }

    @Override
    public AuthContext process(AuthContext context) {
        String token = cookieService.getChallenge(context.getRequest());

        VerificationToken challenge = tokenService.findValidVerificationToken(token, TokenType.CHALLENGE);
        // code 202
        if (challenge.getStatus() == VerificationTokenStatus.PENDING) {
            throw new TokenException(TokenType.CHALLENGE, TokenFailureReason.PENDING);
        }
        // invalid
        if (challenge.getStatus() != VerificationTokenStatus.VERIFIED) {
            throw new TokenException(TokenType.CHALLENGE, TokenFailureReason.INVALID);
        }
        challenge.setStatus(VerificationTokenStatus.CONSUMED);
        context.setUser(challenge.getUser());
        context.setRememberMe(Boolean.parseBoolean(challenge.getMetadata()));
        tokenService.save(challenge);
        return context;
    }

}
