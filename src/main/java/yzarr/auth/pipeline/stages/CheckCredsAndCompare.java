package yzarr.auth.pipeline.stages;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import yzarr.auth.model.AuthException;
import yzarr.auth.model.User;
import yzarr.auth.model.enums.ErrorCode;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.service.UserService;

@Component
@RequiredArgsConstructor
public class CheckCredsAndCompare implements AuthStage {
    private final UserService userService;

    @Override
    public AuthContext process(AuthContext context) {
        User user = userService.findUser(context.getEmail());
        userService.matches(context.getPassword(), user);
        if (!user.getEmail().equals(context.getUser().getEmail())) {
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }
        return context;
    }

}
