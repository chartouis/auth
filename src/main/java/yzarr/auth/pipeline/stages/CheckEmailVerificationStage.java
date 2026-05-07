package yzarr.auth.pipeline.stages;

import yzarr.auth.model.AuthException;
import yzarr.auth.model.enums.ErrorCode;
import yzarr.auth.pipeline.AuthContext;

public class CheckEmailVerificationStage implements AuthStage {

    @Override
    public AuthContext process(AuthContext context) {
        if (!context.getUser().isEmailVerified()) {
            throw new AuthException(ErrorCode.EMAIL_IS_NOT_VERIFIED);
        }
        return context;
    }

}
