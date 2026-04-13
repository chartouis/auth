package yzarr.auth.config;

import org.springframework.stereotype.Component;

import yzarr.auth.model.stages.AuthenticationStage;
import yzarr.auth.model.stages.CreateAccountStage;
import yzarr.auth.model.stages.RefreshTokenIssueStage;
import yzarr.auth.model.stages.ValidEmailPasswordStage;

@Component
public class AuthPipelineFactory {

    private final CreateAccountStage createAccountStage;
    private final ValidEmailPasswordStage validEmailPasswordStage;
    private final AuthenticationStage authenticationStage;
    private final RefreshTokenIssueStage refreshTokenIssueStage;

    public AuthPipelineFactory(CreateAccountStage createAccountStage, ValidEmailPasswordStage validEmailPasswordStage,
            AuthenticationStage authenticationStage, RefreshTokenIssueStage refreshTokenIssueStage) {
        this.createAccountStage = createAccountStage;
        this.validEmailPasswordStage = validEmailPasswordStage;
        this.authenticationStage = authenticationStage;
        this.refreshTokenIssueStage = refreshTokenIssueStage;
    }

    public AuthPipeline createRegister() {
        return new AuthPipeline()
                .add(validEmailPasswordStage)
                .add(createAccountStage);
    }

    public AuthPipeline createLogin() {
        return new AuthPipeline()
                .add(validEmailPasswordStage)
                .add(authenticationStage)
                .add(refreshTokenIssueStage);
    }
}
