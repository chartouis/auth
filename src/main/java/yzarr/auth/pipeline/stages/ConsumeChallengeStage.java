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
import yzarr.auth.service.CookieService;
import yzarr.auth.service.TokenService;

@Component
@Slf4j
public class ConsumeChallengeStage implements AuthStage {
    private final CookieService cookieService;
    private final TokenService tokenService;
    private final VerificationTokenRepo verificationTokenRepo;

    public ConsumeChallengeStage(CookieService cookieService, TokenService tokenService,
            VerificationTokenRepo verificationTokenRepo) {
        this.cookieService = cookieService;
        this.tokenService = tokenService;
        this.verificationTokenRepo = verificationTokenRepo;
    }

    @Override
    public AuthContext process(AuthContext context) {
        String token = cookieService.getChallenge(context.getRequest());
        if (token == null) {
            throw new TokenException(TokenType.CHALLENGE, TokenFailureReason.MISSING);
        }
        Optional<VerificationToken> ochallenge = tokenService.getVerificationTokenByTokenHash(token);
        if (ochallenge.isEmpty()) {
            throw new TokenException(TokenType.CHALLENGE, TokenFailureReason.INVALID);
        }
        VerificationToken challenge = ochallenge.get();
        // code 202
        if (challenge.getStatus() == Status.PENDING) {
            throw new TokenException(TokenType.CHALLENGE, TokenFailureReason.PENDING);
        }

        if (challenge.getStatus() == Status.EXPIRED) {
            throw new TokenException(TokenType.CHALLENGE, TokenFailureReason.EXPIRED);
        }
        // invalid
        if (challenge.getStatus() != Status.VERIFIED || challenge.getType() != TokenType.CHALLENGE) {
            throw new TokenException(TokenType.CHALLENGE, TokenFailureReason.INVALID);
        }
        challenge.setStatus(Status.CONSUMED);
        context.setUser(challenge.getUser());
        verificationTokenRepo.save(challenge);
        return context;
    }

}
