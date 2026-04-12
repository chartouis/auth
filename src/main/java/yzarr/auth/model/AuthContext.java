package yzarr.auth.model;

import lombok.Data;
import yzarr.auth.AuthProperties;

@Data
public class AuthContext {
    private String email;
    private String password;
    private User user;
    private AuthProperties props;
    private boolean stop = false;

    public AuthContext(String email, String password, AuthProperties props) {
        this.email = email;
        this.password = password;
        this.props = props;

    }

    public AuthContext stop() {
        this.stop = true;
        return this;
    }
    // private boolean isEmailVerified;
    // private boolean is2FAVerified;
}
