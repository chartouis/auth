package yzarr.auth.config;

import org.springframework.stereotype.Component;

import yzarr.auth.model.stages.AccessTokenIssueStage;
import yzarr.auth.model.stages.AuthenticationStage;
import yzarr.auth.model.stages.CreateAccountStage;
import yzarr.auth.model.stages.EmailVerificationStage;
import yzarr.auth.model.stages.RefreshTokenIssueStage;
import yzarr.auth.model.stages.ValidEmailPasswordStage;
import yzarr.auth.model.stages.VerifyEmailVerificationTokenStage;
import yzarr.auth.model.stages.VerifyRefreshTokenStage;

@Component
public class AuthPipelineFactory {

    private final CreateAccountStage createAccountStage;
    private final ValidEmailPasswordStage validEmailPasswordStage;
    private final AuthenticationStage authenticationStage;
    private final RefreshTokenIssueStage refreshTokenIssueStage;
    private final VerifyRefreshTokenStage verifyRefreshTokenStage;
    private final AccessTokenIssueStage accessTokenIssueStage;
    private final EmailVerificationStage emailVerificationStage;
    private final VerifyEmailVerificationTokenStage verifyEmailVerificationTokenStage;

    public AuthPipelineFactory(CreateAccountStage createAccountStage, ValidEmailPasswordStage validEmailPasswordStage,
            AuthenticationStage authenticationStage, RefreshTokenIssueStage refreshTokenIssueStage,
            VerifyRefreshTokenStage verifyRefreshTokenStage, AccessTokenIssueStage accessTokenIssueStage,
            EmailVerificationStage emailVerificationStage,
            VerifyEmailVerificationTokenStage verifyEmailVerificationTokenStage) {
        this.createAccountStage = createAccountStage;
        this.validEmailPasswordStage = validEmailPasswordStage;
        this.authenticationStage = authenticationStage;
        this.refreshTokenIssueStage = refreshTokenIssueStage;
        this.verifyRefreshTokenStage = verifyRefreshTokenStage;
        this.accessTokenIssueStage = accessTokenIssueStage;
        this.emailVerificationStage = emailVerificationStage;
        this.verifyEmailVerificationTokenStage = verifyEmailVerificationTokenStage;
    }

    /**
     * Register Pipeline. Needs these params in context to work, then creates a user
     * 
     * @param email
     * @param password
     * @param props
     */
    public AuthPipeline createRegister() {
        return new AuthPipeline()
                .add(validEmailPasswordStage)
                .add(createAccountStage)
                .add(emailVerificationStage);
    }

    /**
     * Login Pipeline. Needs these params in context to work. Returns
     * refresh-token as cookie in the response
     * 
     * @param email
     * @param password
     * @param props
     * @param rememberMe
     * 
     */
    public AuthPipeline createLogin() {
        return new AuthPipeline()
                .add(validEmailPasswordStage)
                .add(authenticationStage)
                .add(refreshTokenIssueStage);
    }

    public AuthPipeline createRefresh() {
        return new AuthPipeline()
                .add(verifyRefreshTokenStage)
                .add(accessTokenIssueStage);
    }

    public AuthPipeline createVerifyEmail() {
        return new AuthPipeline()
                .add(verifyEmailVerificationTokenStage);
    }

}
