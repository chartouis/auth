package yzarr.auth.config;

import org.springframework.stereotype.Service;

import yzarr.auth.AuthProperties;
import yzarr.auth.model.AuthContext;

@Service
public class UserService {

    private final AuthProperties props;
    private final AuthPipeline registerPipeline;

    public UserService(AuthPipelineFactory factory, AuthProperties props) {
        registerPipeline = factory.createRegister();
        this.props = props;
    }

    public void register(String email, String password) {
        registerPipeline.execute(new AuthContext(email, password, props));
    }
}
