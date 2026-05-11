package yzarr.auth.pipeline.stages;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yzarr.auth.model.RefreshToken;
import yzarr.auth.model.enums.RefreshTokenStatus;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.repo.RefreshTokenRepo;

@RequiredArgsConstructor
@Component
@Slf4j
public class RevokeRefreshTokensStage implements AuthStage {

    private final RefreshTokenRepo refreshTokenRepo;

    @Override
    public AuthContext process(AuthContext context) {
        List<RefreshToken> list = refreshTokenRepo.findByUserAndStatus(context.getUser(), RefreshTokenStatus.ACTIVE);
        for (RefreshToken token : list) {
            token.revoke(context.getRevokeReason());
        }
        refreshTokenRepo.saveAll(list);
        log.info("Revoked refresh tokens: userId={} count={}", context.getUser().getId(), list.size());
        return context;
    }
}
