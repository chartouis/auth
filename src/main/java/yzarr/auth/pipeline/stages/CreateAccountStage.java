package yzarr.auth.pipeline.stages;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import yzarr.auth.model.AuthException;
import yzarr.auth.model.User;
import yzarr.auth.model.enums.ErrorCode;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.repo.UserRepo;

/* Creates a user, given the email and password are okay */
@Component
@Slf4j
public class CreateAccountStage implements AuthStage {

    private final BCryptPasswordEncoder encoder;
    private final UserRepo repo;

    public CreateAccountStage(UserRepo repo, BCryptPasswordEncoder encoder) {
        this.encoder = encoder;
        this.repo = repo;
    }

    @Override
    public AuthContext process(AuthContext context) {
        if (exists(context.getEmail())) {
            throw new AuthException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        User user = new User(context.getEmail(), encoder.encode(context.getPassword()));
        if (!context.getProps().isEmailVerification()) {
            user.setEmailVerified(true);
        }

        context.setUser(repo.save(user));
        log.info("Created User : {}", user.getUsername());

        return context;
    }

    private boolean exists(String email) {
        return repo.existsByEmail(email);
    }

}
