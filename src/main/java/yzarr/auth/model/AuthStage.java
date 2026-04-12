package yzarr.auth.model;

public interface AuthStage {

    AuthContext process(AuthContext context);

}
