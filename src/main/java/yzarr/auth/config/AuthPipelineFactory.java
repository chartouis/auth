package yzarr.auth.config;

import org.springframework.stereotype.Component;

import yzarr.auth.model.stages.CreateAccountStage;
import yzarr.auth.model.stages.ValidEmailPasswordStage;

@Component
public class AuthPipelineFactory {

    private final CreateAccountStage createAccountStage;
    private final ValidEmailPasswordStage validEmailPasswordStage;

    public AuthPipelineFactory(ValidEmailPasswordStage validEmailPasswordStage, CreateAccountStage createAccountStage) {
        this.createAccountStage = createAccountStage;
        this.validEmailPasswordStage = validEmailPasswordStage;

    }

    public AuthPipeline createRegister() {
        return new AuthPipeline()
                .add(validEmailPasswordStage)
                .add(createAccountStage);
    }
}
