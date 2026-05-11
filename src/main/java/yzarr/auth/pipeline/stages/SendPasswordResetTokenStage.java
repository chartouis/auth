package yzarr.auth.pipeline.stages;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import yzarr.auth.model.enums.TokenType;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.service.MailService;
import yzarr.auth.service.TokenService;
import yzarr.auth.service.UserService;

@Component
@RequiredArgsConstructor
public class SendPasswordResetTokenStage implements AuthStage {
    private final TokenService tokenService;
    private final MailService mailService;
    private final UserService userService;

    @Override
    public AuthContext process(AuthContext context) {
        context.setUser(userService.findUser(context.getEmail()));
        String token = tokenService.generatePasswordResetToken(context.getUser());
        mailService.sendTokenEmail(TokenType.PASSWORD_RESET, token, context.getEmail(), "/auth/verify/password-reset");
        return context;
    }

}
