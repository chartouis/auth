package yzarr.auth.pipeline.stages;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import yzarr.auth.model.RefreshToken;
import yzarr.auth.model.enums.RevokeReason;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.repo.RefreshTokenRepo;

@RequiredArgsConstructor
@Component
public class RevokeRefreshTokensStage implements AuthStage {

    private final RefreshTokenRepo refreshTokenRepo;
    private RevokeReason reason = RevokeReason.PASSWORD_CHANGED;

    @Override
    public AuthContext process(AuthContext context) {
        List<RefreshToken> list = refreshTokenRepo.findByUser(context.getUser());
        for (RefreshToken token : list) {
            token.revoke(reason);
            refreshTokenRepo.save(token);
        }
        return context;
    }

    public void setReason(RevokeReason reason) {
        this.reason = reason;
    }

}
