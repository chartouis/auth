package yzarr.auth.pipeline.stages;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import yzarr.auth.model.AuthException;
import yzarr.auth.model.User;
import yzarr.auth.model.enums.ErrorCode;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.repo.UserRepo;

/* fetches User object from repo if exists, then matches the passwords */
@Component
@Slf4j
public class AuthenticationStage implements AuthStage {

    private final UserRepo repo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public AuthenticationStage(UserRepo repo) {
        this.repo = repo;
    }

    @Override
    public AuthContext process(AuthContext context) {

        Optional<User> ouser = repo.findByEmail(context.getEmail());
        if (ouser.isEmpty()) {
            context.stop();
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }
        context.setUser(ouser.get());

        if (!encoder.matches(context.getPassword(), context.getUser().getPasswordHash())) {
            context.stop();
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }

        return context;
    }

}
