package yzarr.auth.pipeline;

import java.util.ArrayList;
import java.util.List;

import yzarr.auth.pipeline.stages.AuthStage;

public class AuthPipeline {
    private List<AuthStage> stages = new ArrayList<>();

    public AuthPipeline add(AuthStage stage) {
        stages.add(stage);
        return this;
    }

    public AuthContext execute(AuthContext context) {
        for (AuthStage stage : stages) {
            if (context.isStop()) {
                break;
            }
            context = stage.process(context);
        }
        return context;
    }
}
