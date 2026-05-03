package yzarr.auth.pipeline.stages;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.repo.UserRepo;
import yzarr.auth.service.UserService;

/* fetches User object from repo if exists, then matches the passwords */
@Component
@Slf4j
public class AuthenticationStage implements AuthStage {
    private final UserService userService;

    public AuthenticationStage(UserRepo repo, UserService userService) {
        this.userService = userService;
    }

    @Override
    public AuthContext process(AuthContext context) {
        context.setUser(userService.findUser(context.getEmail()));
        userService.matches(context.getPassword(), context.getUser());
        return context;
    }

}
