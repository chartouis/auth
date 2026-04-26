package yzarr.auth.pipeline.stages;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import yzarr.auth.model.TokenException;
import yzarr.auth.model.VerificationToken;
import yzarr.auth.model.enums.VerificationTokenStatus;
import yzarr.auth.model.enums.TokenFailureReason;
import yzarr.auth.model.enums.TokenType;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.service.TokenService;

/* requires twofa token in context */
@Component
@Slf4j
public class VerifyChallengeStage implements AuthStage {
    private final TokenService tokenService;

    public VerifyChallengeStage(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public AuthContext process(AuthContext context) {
        String stwofa = context.getToken();
        VerificationToken twofa = tokenService.findValidVerificationToken(stwofa, TokenType.TWO_FACTOR);
        VerificationToken challenge = tokenService.findValidVerificationToken(twofa.getMetadata(), TokenType.CHALLENGE,
                true);
        if (twofa.getStatus() != VerificationTokenStatus.PENDING) {
            throw new TokenException(TokenType.TWO_FACTOR, TokenFailureReason.INVALID);
        }

        challenge.setStatus(VerificationTokenStatus.VERIFIED);
        twofa.setStatus(VerificationTokenStatus.CONSUMED);

        tokenService.save(challenge);
        tokenService.save(twofa);
        return context;

    }

}
