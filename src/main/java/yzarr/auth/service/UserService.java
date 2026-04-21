package yzarr.auth.service;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import yzarr.auth.AuthProperties;
import yzarr.auth.pipeline.AuthContext;
import yzarr.auth.pipeline.AuthPipeline;
import yzarr.auth.pipeline.AuthPipelineFactory;

@Service
public class UserService {

    private final AuthProperties props;
    private final AuthPipeline registerPipeline;
    private final AuthPipeline loginPipeline;
    private final AuthPipeline refreshPipeline;
    private final AuthPipeline verifyEmailPipeline;

    public UserService(AuthPipelineFactory factory, AuthProperties props) {
        registerPipeline = factory.createRegister();
        loginPipeline = factory.createLogin();
        refreshPipeline = factory.createRefresh();
        this.verifyEmailPipeline = factory.createVerifyEmail();
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

    public void refresh(HttpServletResponse response, HttpServletRequest request) {
        refreshPipeline.execute(AuthContext.builder()
                .request(request)
                .response(response)
                .props(props)
                .build());
    }

    public void verifyEmail(String token) {
        verifyEmailPipeline.execute(AuthContext.builder()
                .token(token)
                .build());
    }
}
