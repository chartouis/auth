package yzarr.auth.pipeline.stages;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.service.UserService;

/* fetches User object from repo if exists, then matches the passwords */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationStage implements AuthStage {
    private final UserService userService;

    @Override
    public AuthContext process(AuthContext context) {
        context.setUser(userService.findUser(context.getEmail()));
        userService.matches(context.getPassword(), context.getUser());
        return context;
    }

}
