package yzarr.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@ConfigurationProperties(prefix = "yzarr.auth")
@Component
@Data
public class AuthProperties {
    // Must be changed in the application.properties
    private String JwtSecret = "default";

    // RememberMe
    private Long RefreshTokenExpiryMs = 1000L * 60L * 60L * 24L * 30L;
    private Long ShortRefreshTokenExpiryMs = 1000L * 60L * 60L;
    private Long AccessTokenExpiryMs = 1000L * 60L * 15L;
    private boolean EnableAuth = true;
    private boolean emailVerification = false;

    // if true, then email verification should also be true
    private boolean TwoFA = false;

    private boolean OAuth = false;

    // Cookie attributes
    private String sameSite = "Strict";
    private boolean httpOnly = true;
    private boolean secure = true;

    // Must be set to something in the application.properties
    private String frontendUrl = "";

    private short MinPasswordLength = 8;
}
