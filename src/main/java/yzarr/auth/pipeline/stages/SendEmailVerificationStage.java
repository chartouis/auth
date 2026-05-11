package yzarr.auth.pipeline.stages;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yzarr.auth.model.enums.TokenType;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.service.MailService;
import yzarr.auth.service.TokenService;

//Checks if verified. Generates token, sends it to user email as link with a param.
@Component
@Slf4j
@RequiredArgsConstructor
public class SendEmailVerificationStage implements AuthStage {
    private final TokenService tokenService;
    private final MailService mailService;

    @Override
    public AuthContext process(AuthContext context) {
        String token = tokenService.generateEmailVerificationToken(context.getUser());
        mailService.sendTokenEmail(TokenType.EMAIL_VERIFICATION, token, context.getEmail(), "/auth/verify/email");
        return context;
    }

}
