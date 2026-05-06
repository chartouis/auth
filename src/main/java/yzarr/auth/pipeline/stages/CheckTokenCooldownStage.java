package yzarr.auth.pipeline.stages;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import yzarr.auth.model.AuthException;
import yzarr.auth.model.VerificationToken;
import yzarr.auth.model.enums.ErrorCode;
import yzarr.auth.model.enums.TokenType;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.repo.VerificationTokenRepo;
import yzarr.auth.service.UserService;

@Component
@RequiredArgsConstructor
public class CheckTokenCooldownStage implements AuthStage {

    private TokenType tokenType;
    private final UserService userService;
    private final VerificationTokenRepo verificationTokenRepo;

    @Override
    public AuthContext process(AuthContext context) {
        if (context.getUser() == null) {
            context.setUser(userService.findUser(context.getEmail()));
        }
        Optional<VerificationToken> otoken = verificationTokenRepo
                .findTopByUserAndTypeOrderByIssuedAtDesc(context.getUser(), tokenType);
        if (otoken.isPresent()) {
            VerificationToken token = otoken.get();
            isBeforeCooldown(token, context.getProps().getTokenSendCooldownMs());
        }

        return context;
    }

    public static void isBeforeCooldown(VerificationToken token, Long cooldownms) {
        if (token.getIssuedAt().isAfter(Instant.now().minusMillis(cooldownms))) {
            throw new AuthException(ErrorCode.EMAIL_ALREADY_SENT);
        }
    }

    public CheckTokenCooldownStage TokenType(TokenType tokenType) {
        this.tokenType = tokenType;
        return this;
    }

}
