package yzarr.auth.pipeline.stages;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import yzarr.auth.model.VerificationToken;
import yzarr.auth.model.enums.TokenType;
import yzarr.auth.model.enums.VerificationTokenStatus;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.service.TokenService;

@Component
@RequiredArgsConstructor
public class VerifyPasswordResetTokenStage implements AuthStage {
    private final TokenService tokenService;

    @Override
    public AuthContext process(AuthContext context) {
        VerificationToken token = tokenService.findValidVerificationToken(context.getToken(), TokenType.PASSWORD_RESET);
        token.setStatus(VerificationTokenStatus.CONSUMED);
        context.setUser(token.getUser());
        tokenService.save(token);
        return context;
    }

}
