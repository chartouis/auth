package yzarr.auth.pipeline.stages;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.service.CookieService;
import yzarr.auth.service.MailService;
import yzarr.auth.service.TokenService;

/* Sets a cookie named challenge
    requires user and response in context*/
@Component
@Slf4j
public class SetChallengeAndSend2FAstage implements AuthStage {
    private final TokenService tokenService;
    private final CookieService cookieService;
    private final MailService mailService;

    public SetChallengeAndSend2FAstage(TokenService tokenService, CookieService cookieService,
            MailService mailService) {
        this.tokenService = tokenService;
        this.cookieService = cookieService;
        this.mailService = mailService;
    }

    @Override
    public AuthContext process(AuthContext context) {
        String twofa = tokenService.generate2FAtoken(context.getUser());
        mailService.sendEmailVerificationMessage(twofa, context.getEmail(), "/auth/verify/2fa");
        String challenge = tokenService.generateChallengeToken(context.getUser(), twofa);
        cookieService.setChallengeCookie(challenge, context.getResponse());
        return context;
    }

}
