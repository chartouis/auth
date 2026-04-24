package yzarr.auth.pipeline.stages;

import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import yzarr.auth.model.TokenException;
import yzarr.auth.model.VerificationToken;
import yzarr.auth.model.enums.Status;
import yzarr.auth.model.enums.TokenFailureReason;
import yzarr.auth.model.enums.TokenType;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.repo.VerificationTokenRepo;
import yzarr.auth.service.TokenService;

/* requires twofa token in context */
@Component
@Slf4j
public class VerifyChallengeStage implements AuthStage {
    private final TokenService tokenService;
    private final VerificationTokenRepo verificationTokenRepo;

    public VerifyChallengeStage(TokenService tokenService, VerificationTokenRepo verificationTokenRepo) {
        this.tokenService = tokenService;
        this.verificationTokenRepo = verificationTokenRepo;
    }

    @Override
    public AuthContext process(AuthContext context) {
        String stwofa = context.getToken();
        if (context.getToken() == null) {
            throw new TokenException(TokenType.TWO_FACTOR, TokenFailureReason.MISSING);
        }
        Optional<VerificationToken> otwofa = tokenService.getVerificationTokenByTokenHash(stwofa);
        Optional<VerificationToken> ochallenge = tokenService.getVerificationTokenByOther(stwofa);
        if (ochallenge.isEmpty() || otwofa.isEmpty()) {
            throw new TokenException(TokenType.TWO_FACTOR, TokenFailureReason.INVALID);
        }
        VerificationToken twofa = otwofa.get();
        VerificationToken challenge = ochallenge.get();
        if (challenge.getType() != TokenType.CHALLENGE || challenge.getTokenHash() == null
                || twofa.getType() != TokenType.TWO_FACTOR || twofa.getStatus() != Status.PENDING) {
            throw new TokenException(TokenType.TWO_FACTOR, TokenFailureReason.INVALID);
        }

        challenge.setStatus(Status.VERIFIED);
        twofa.setStatus(Status.CONSUMED);

        verificationTokenRepo.save(challenge);
        verificationTokenRepo.save(twofa);
        return context;

    }

}
