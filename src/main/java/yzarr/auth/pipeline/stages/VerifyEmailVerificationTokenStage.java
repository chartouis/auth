package yzarr.auth.pipeline.stages;

import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import yzarr.auth.model.AuthException;
import yzarr.auth.model.User;
import yzarr.auth.model.enums.ErrorCode;
import yzarr.auth.model.enums.TokenType;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.repo.UserRepo;
import yzarr.auth.service.TokenService;

//Checks token, if exists then switches emailVerified of user to true
@Component
@Slf4j
public class VerifyEmailVerificationTokenStage implements AuthStage {
    private final TokenService tokenService;

    public VerifyEmailVerificationTokenStage(TokenService tokenService, UserRepo userRepo) {
        this.tokenService = tokenService;
        this.userRepo = userRepo;
    }

    private final UserRepo userRepo;

    @Override
    public AuthContext process(AuthContext context) {
        if (context.getToken() == null) {
            throw new AuthException(ErrorCode.NO_EMAIL_VERIFICATION_TOKEN);
        }
        Optional<User> ouser = tokenService.getUserByToken(context.getToken(), TokenType.EMAIL_VERIFICATION);
        if (ouser.isEmpty()) {
            throw new AuthException(ErrorCode.INVALID_EMAIL_VERIFICATION_TOKEN);
        }
        User user = ouser.get();

        if (user.isEmailVerified()) {
            throw new AuthException(ErrorCode.EMAIL_IS_ALREADY_VERIFIED);
        }
        user.setEmailVerified(true);
        context.setUser(userRepo.save(user));
        return context;

    }

}
