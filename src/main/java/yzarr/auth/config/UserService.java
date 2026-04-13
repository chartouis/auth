package yzarr.auth.config;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import yzarr.auth.AuthProperties;
import yzarr.auth.model.AuthContext;

@Service
public class UserService {

    private final AuthProperties props;
    private final AuthPipeline registerPipeline;
    private final AuthPipeline loginPipeline;

    public UserService(AuthPipelineFactory factory, AuthProperties props) {
        registerPipeline = factory.createRegister();
        loginPipeline = factory.createLogin();
        this.props = props;
    }

    public void register(String email, String password) {
        registerPipeline.execute(AuthContext.builder()
                .email(email)
                .password(password)
                .props(props)
                .build());
    }

    public void login(String email, String password, boolean rememberMe, HttpServletResponse response) {
        loginPipeline.execute(AuthContext.builder()
                .email(email)
                .password(password)
                .props(props)
                .rememberMe(rememberMe)
                .response(response)
                .build());
    }
}
