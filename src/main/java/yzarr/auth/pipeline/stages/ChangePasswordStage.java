package yzarr.auth.pipeline.stages;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.service.UserService;

@Component
@RequiredArgsConstructor
public class ChangePasswordStage implements AuthStage {
    private final UserService userService;

    @Override
    public AuthContext process(AuthContext context) {
        ValidPasswordStage.validatePassword(context.getNewPassword(), context.getProps().getMinPasswordLength());
        context.setUser(userService.changePassword(context.getUser(), context.getNewPassword()));
        return context;
    }

}
