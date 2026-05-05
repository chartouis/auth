package yzarr.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@ConfigurationProperties(prefix = "yzarr.auth")
@Component
@Data
public class AuthProperties {

    // Must be changed in application.properties
    private String jwtSecret = "default";

    // RememberMe
    private Long refreshTokenExpiryMs = 1000L * 60L * 60L * 24L * 30L; // 30 days
    private Long absoluteExpiryMs = 1000L * 60L * 60L * 24L * 90L; // 90 days
    private Long shortAbsoluteExpiryMs = 1000L * 60L * 60L; // 1 hour
    private Long accessTokenExpiryMs = 1000L * 60L * 15L; // 15 minutes
    private Long emailVerificationTokenExpiryMs = 1000L * 60L * 60L;
    private Long passwordResetTokenExpiryMs = 1000L * 60L * 5L;
    private Long challengeTokenExpiryMs = 1000L * 600L;
    private Long twoFactorTokenExpiryMs = 1000L * 600L;

    private boolean enableAuth = true;
    private boolean refreshTokenRotation = true;
    private boolean emailVerification = false;

    // if true, then email verification should also be true
    private boolean twoFa = false;
    private boolean oauth = false;

    // Cookie attributes
    private String sameSite = "Strict";
    private boolean httpOnly = true;
    private boolean secure = true;

    // Must be set in application.properties
    private String frontendUrl = "";
    private String oauthRedirectUrl = frontendUrl;

    private short minPasswordLength = 8;

    // SMTP credentials
    private String appUsername = "default@gmail.com";
    private String appPassword = "default";
}