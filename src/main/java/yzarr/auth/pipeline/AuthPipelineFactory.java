package yzarr.auth.pipeline;

import org.springframework.stereotype.Component;

import yzarr.auth.AuthProperties;
import yzarr.auth.pipeline.stages.AccessTokenIssueStage;
import yzarr.auth.pipeline.stages.AuthenticationStage;
import yzarr.auth.pipeline.stages.ConsumeChallengeStage;
import yzarr.auth.pipeline.stages.CreateAccountStage;
import yzarr.auth.pipeline.stages.EmailVerificationStage;
import yzarr.auth.pipeline.stages.RefreshTokenIssueStage;
import yzarr.auth.pipeline.stages.SetChallengeAndSend2FAstage;
import yzarr.auth.pipeline.stages.ValidEmailPasswordStage;
import yzarr.auth.pipeline.stages.VerifyChallengeStage;
import yzarr.auth.pipeline.stages.VerifyEmailVerificationTokenStage;
import yzarr.auth.pipeline.stages.VerifyRefreshTokenStage;

@Component
public class AuthPipelineFactory {

    private final AuthProperties authProperties;
    private final CreateAccountStage createAccountStage;
    private final ValidEmailPasswordStage validEmailPasswordStage;
    private final AuthenticationStage authenticationStage;
    private final RefreshTokenIssueStage refreshTokenIssueStage;
    private final VerifyRefreshTokenStage verifyRefreshTokenStage;
    private final AccessTokenIssueStage accessTokenIssueStage;
    private final EmailVerificationStage emailVerificationStage;
    private final VerifyEmailVerificationTokenStage verifyEmailVerificationTokenStage;
    private final ConsumeChallengeStage consumeChallengeStage;
    private final SetChallengeAndSend2FAstage setChallengeAndSend2FAstage;
    private final VerifyChallengeStage verifyChallengeStage;

    public AuthPipelineFactory(CreateAccountStage createAccountStage, ValidEmailPasswordStage validEmailPasswordStage,
            AuthenticationStage authenticationStage, RefreshTokenIssueStage refreshTokenIssueStage,
            VerifyRefreshTokenStage verifyRefreshTokenStage, AccessTokenIssueStage accessTokenIssueStage,
            EmailVerificationStage emailVerificationStage,
            VerifyEmailVerificationTokenStage verifyEmailVerificationTokenStage,
            ConsumeChallengeStage consumeChallengeStage, SetChallengeAndSend2FAstage setChallengeAndSend2FAstage,
            SetChallengeAndSend2FAstage setChallengeAndSend2FAstage2, VerifyChallengeStage verifyChallengeStage,
            AuthProperties authProperties) {
        this.createAccountStage = createAccountStage;
        this.validEmailPasswordStage = validEmailPasswordStage;
        this.authenticationStage = authenticationStage;
        this.refreshTokenIssueStage = refreshTokenIssueStage;
        this.verifyRefreshTokenStage = verifyRefreshTokenStage;
        this.accessTokenIssueStage = accessTokenIssueStage;
        this.emailVerificationStage = emailVerificationStage;
        this.verifyEmailVerificationTokenStage = verifyEmailVerificationTokenStage;
        this.consumeChallengeStage = consumeChallengeStage;
        this.setChallengeAndSend2FAstage = setChallengeAndSend2FAstage2;
        this.verifyChallengeStage = verifyChallengeStage;
        this.authProperties = authProperties;
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
        AuthPipeline pipeline = new AuthPipeline()
                .add(validEmailPasswordStage)
                .add(authenticationStage);
        if (authProperties.isTwoFa()) {
            pipeline.add(setChallengeAndSend2FAstage);
        } else {
            pipeline.add(refreshTokenIssueStage);
        }
        return pipeline;
    }

    public AuthPipeline createRefresh() {
        return new AuthPipeline()
                .add(verifyRefreshTokenStage)
                .add(accessTokenIssueStage);
    }

    public AuthPipeline createVerifyEmail() {
        return new AuthPipeline().add(verifyEmailVerificationTokenStage);
    }

    public AuthPipeline createVerify2fa() {
        return new AuthPipeline().add(verifyChallengeStage);
    }

    public AuthPipeline createCheck2faStatus() {
        return new AuthPipeline()
                .add(consumeChallengeStage)
                .add(refreshTokenIssueStage);
    }

}
