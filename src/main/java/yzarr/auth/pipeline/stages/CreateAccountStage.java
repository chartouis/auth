package yzarr.auth.pipeline.stages;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.service.UserService;

/* Creates a user, given the email and password are okay */
@Component
@Slf4j
@RequiredArgsConstructor
public class CreateAccountStage implements AuthStage {

    private final UserService userService;

    @Override
    public AuthContext process(AuthContext context) {

        context.setUser(userService.createUser(context.getEmail(), context.getPassword()));
        log.info("Created User : {}", context.getUser().getUsername());

        return context;
    }
}
