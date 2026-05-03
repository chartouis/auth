package yzarr.auth.core;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import yzarr.auth.model.User;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.pipeline.AuthPipeline;
import yzarr.auth.pipeline.stages.AccessTokenIssueStage;
import yzarr.auth.pipeline.stages.RefreshTokenIssueStage;
import yzarr.auth.service.UserService;

@Component
public class OauthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserService userService;
    private final RefreshTokenIssueStage refreshTokenIssueStage;
    private final AccessTokenIssueStage accessTokenIssueStage;

    public OauthSuccessHandler(UserService userService, AccessTokenIssueStage accessTokenIssueStage,
            RefreshTokenIssueStage refreshTokenIssueStage) {
        this.userService = userService;
        this.refreshTokenIssueStage = refreshTokenIssueStage;
        this.accessTokenIssueStage = accessTokenIssueStage;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        User user = userService.findUser(oidcUser.getEmail());
        AuthPipeline pipeline = new AuthPipeline().add(refreshTokenIssueStage).add(accessTokenIssueStage);
        pipeline.execute(AuthContext.builder().user(user).response(response).build());
        clearAuthenticationAttributes(request);

        getRedirectStrategy().sendRedirect(request, response,
                "https://httpbin.org/get?cooks=" + response.getHeaders("Set-Cookie"));
    }

}
