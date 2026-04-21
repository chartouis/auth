package yzarr.auth.pipeline.stages;

import yzarr.auth.pipeline.AuthContext;

public interface AuthStage {

    AuthContext process(AuthContext context);

}
