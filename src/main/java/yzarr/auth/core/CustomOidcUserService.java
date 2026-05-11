package yzarr.auth.core;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yzarr.auth.model.enums.AuthProvider;
import yzarr.auth.service.UserService;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomOidcUserService extends OidcUserService {
    private final UserService userService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        if (!userService.exists(oidcUser.getEmail())) {
            log.info("OAuth user created: email={}", oidcUser.getEmail());
            userService.createUserOauth(
                    oidcUser.getEmail(),
                    oidcUser.getFullName(),
                    AuthProvider.valueOf(userRequest
                            .getClientRegistration()
                            .getRegistrationId()
                            .toUpperCase()));
        } else {
            log.info("OAuth user exists: email={}", oidcUser.getEmail());
            userService.verifyEmailIfNot(oidcUser.getEmail());
        }

        return oidcUser;
    }
}
