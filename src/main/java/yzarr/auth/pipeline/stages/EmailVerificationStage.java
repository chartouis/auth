package yzarr.auth.pipeline.stages;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import yzarr.auth.model.AuthException;
import yzarr.auth.model.enums.ErrorCode;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.service.MailService;
import yzarr.auth.service.TokenService;

//Checks if verified. Generates token, sends it to user email as link with a param.
@Component
@Slf4j
public class EmailVerificationStage implements AuthStage {
    private final TokenService tokenService;
    private final MailService mailService;

    public EmailVerificationStage(TokenService tokenService, MailService mailService) {
        this.tokenService = tokenService;
        this.mailService = mailService;
    }

    @Override
    public AuthContext process(AuthContext context) {
        if (context.getUser().isEmailVerified()) {
            return context;
        }

        String token = tokenService.generateEmailVerificationToken(context.getUser());
        mailService.sendEmailVerificationMessage(token, context.getEmail(), "/auth/verify/email");
        throw new AuthException(ErrorCode.EMAIL_IS_NOT_VERIFIED);
    }

}
