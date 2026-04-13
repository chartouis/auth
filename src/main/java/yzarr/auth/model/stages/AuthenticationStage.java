package yzarr.auth.model.stages;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import yzarr.auth.UserRepo;
import yzarr.auth.model.AuthContext;
import yzarr.auth.model.AuthStage;
import yzarr.auth.model.User;

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
            return context.stop();
        }
        context.setUser(ouser.get());

        if (!encoder.matches(context.getPassword(), context.getUser().getPasswordHash())) {
            return context.stop();
        }

        return context;
    }

}
