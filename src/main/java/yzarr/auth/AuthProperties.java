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
    private Long RefreshTokenExpiryMs = 1000L * 60L * 60L * 24L * 30L; // 30days
    private Long AbsoluteExpiryMs = 1000L * 60L * 60L * 24L * 90L; // 90days
    private Long ShortAbsoluteExpiryMs = 1000L * 60L * 60L; // 1hour
    private Long AccessTokenExpiryMs = 1000L * 60L * 15L; // 15 minutes
    private Long EmailVerificationTokenExpiryMs = 1000L * 60L * 60L;
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

    // SMTP password and app name
    private String appUsername = "default@gmail.com";
    private String appPassword = "default";
}
