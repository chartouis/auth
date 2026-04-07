package yzarr.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "yzarr.auth")
@Data
public class AuthProperties {
    private String JwtSecret = "default";
    private Long RefreshTokenExpiryMs = 1000L * 60L * 60L * 24L * 30L;
    private Long AccessTokenExpiryMs = 1000L * 60L * 15L;
    private boolean EnableAuth = true;
    private boolean OAuth = false;
}
