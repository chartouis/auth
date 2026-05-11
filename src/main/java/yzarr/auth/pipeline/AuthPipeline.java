package yzarr.auth.pipeline;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import yzarr.auth.pipeline.stages.AuthStage;

@Slf4j
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
            log.debug("Executing stage: {}", stage.getClass().getSimpleName());
            context = stage.process(context);
        }
        return context;
    }
}
