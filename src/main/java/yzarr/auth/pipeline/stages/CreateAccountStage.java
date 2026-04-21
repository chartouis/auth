package yzarr.auth.pipeline.stages;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import yzarr.auth.model.User;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.repo.UserRepo;

/* Creates a user, given the email and password are okay */
@Component
@Slf4j
public class CreateAccountStage implements AuthStage {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final UserRepo repo;

    @Override
    public AuthContext process(AuthContext context) {
        if (exists(context.getEmail())) {
            return context.stop();
        }
        User user = new User(context.getEmail(), encoder.encode(context.getPassword()));
        if (!context.getProps().isEmailVerification()) {
            user.setEmailVerified(true);
        }

        context.setUser(repo.save(user));
        log.info("Created User : {}", user.getUsername());

        return context;
    }

    public CreateAccountStage(UserRepo repo) {
        this.repo = repo;
    }

    private boolean exists(String email) {
        return repo.existsByEmail(email);
    }

}
