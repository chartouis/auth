package yzarr.auth.config;

import org.springframework.stereotype.Service;

import yzarr.auth.AuthProperties;
import yzarr.auth.model.AuthContext;

@Service
public class UserService {

    private final AuthPipelineFactory factory;
    private final AuthProperties props;

    public UserService(AuthPipelineFactory factory, AuthProperties props) {
        this.factory = factory;
        this.props = props;
    }

    public void register(String email, String password) {
        factory.createRegister().execute(new AuthContext(email, password, props));
    }
}
