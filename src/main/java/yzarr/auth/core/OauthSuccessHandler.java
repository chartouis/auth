package yzarr.auth.core;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yzarr.auth.AuthProperties;
import yzarr.auth.model.User;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.pipeline.AuthPipeline;
import yzarr.auth.pipeline.stages.AccessTokenIssueStage;
import yzarr.auth.pipeline.stages.RefreshTokenIssueStage;
import yzarr.auth.service.UserService;

@Component
@RequiredArgsConstructor
@Slf4j
public class OauthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserService userService;
    private final RefreshTokenIssueStage refreshTokenIssueStage;
    private final AccessTokenIssueStage accessTokenIssueStage;
    private final AuthProperties props;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        try {
            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
            log.info("OAuth login success: email={}", oidcUser.getEmail());
            User user = userService.findUser(oidcUser.getEmail());
            AuthPipeline pipeline = new AuthPipeline().add(refreshTokenIssueStage).add(accessTokenIssueStage);
            pipeline.execute(AuthContext.builder().props(props).user(user).response(response).build());
            clearAuthenticationAttributes(request);

            getRedirectStrategy().sendRedirect(request, response, props.getOauthRedirectUrl());
        } catch (Exception ex) {
            log.error("OAuth success handler failed unexpectedly", ex);
            throw ex;
        }
    }

}
