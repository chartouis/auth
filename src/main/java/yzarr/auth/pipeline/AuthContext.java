package yzarr.auth.pipeline;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import yzarr.auth.AuthProperties;
import yzarr.auth.model.User;
import yzarr.auth.model.enums.AuthProvider;

@Data
@Builder
@AllArgsConstructor
public class AuthContext {
    private String email;
    private String password;
    private String newPassword;
    private User user;
    private String token;
    private AuthProperties props;
    private HttpServletResponse response;
    private HttpServletRequest request;
    private AuthProvider provider;
    @Builder.Default
    private boolean stop = false;
    @Builder.Default
    private boolean rememberMe = false;

    public AuthContext stop() {
        this.stop = true;
        return this;
    }
}
