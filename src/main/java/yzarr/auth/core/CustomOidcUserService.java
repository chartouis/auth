package yzarr.auth.core;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import yzarr.auth.model.enums.AuthProvider;
import yzarr.auth.service.UserService;

@Component
public class CustomOidcUserService extends OidcUserService {
    private final UserService userService;

    public CustomOidcUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        if (!userService.exists(oidcUser.getEmail())) {
            userService.createUserOauth(
                    oidcUser.getEmail(),
                    oidcUser.getFullName(),
                    AuthProvider.valueOf(userRequest
                            .getClientRegistration()
                            .getRegistrationId()
                            .toUpperCase()));
        } else {
            userService.verifyEmailIfNot(oidcUser.getEmail());
        }

        return oidcUser;
    }
}
