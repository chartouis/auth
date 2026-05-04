package yzarr.auth.pipeline;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import yzarr.auth.AuthProperties;
import yzarr.auth.pipeline.stages.AccessTokenIssueStage;
import yzarr.auth.pipeline.stages.AuthenticationStage;
import yzarr.auth.pipeline.stages.ConsumeChallengeStage;
import yzarr.auth.pipeline.stages.CreateAccountStage;
import yzarr.auth.pipeline.stages.EmailVerificationStage;
import yzarr.auth.pipeline.stages.LogoutStage;
import yzarr.auth.pipeline.stages.RefreshTokenIssueStage;
import yzarr.auth.pipeline.stages.RefreshTokenRotationStage;
import yzarr.auth.pipeline.stages.SetChallengeAndSend2FAstage;
import yzarr.auth.pipeline.stages.ValidEmailStage;
import yzarr.auth.pipeline.stages.ValidPasswordStage;
import yzarr.auth.pipeline.stages.VerifyChallengeStage;
import yzarr.auth.pipeline.stages.VerifyEmailVerificationTokenStage;
import yzarr.auth.pipeline.stages.VerifyRefreshTokenStage;

@Component
@RequiredArgsConstructor
public class AuthPipelineFactory {

    private final AuthProperties authProperties;
    private final CreateAccountStage createAccountStage;
    private final ValidPasswordStage validPasswordStage;
    private final ValidEmailStage validEmailStage;
    private final AuthenticationStage authenticationStage;
    private final RefreshTokenIssueStage refreshTokenIssueStage;
    private final VerifyRefreshTokenStage verifyRefreshTokenStage;
    private final AccessTokenIssueStage accessTokenIssueStage;
    private final EmailVerificationStage emailVerificationStage;
    private final VerifyEmailVerificationTokenStage verifyEmailVerificationTokenStage;
    private final ConsumeChallengeStage consumeChallengeStage;
    private final SetChallengeAndSend2FAstage setChallengeAndSend2FAstage;
    private final VerifyChallengeStage verifyChallengeStage;
    private final RefreshTokenRotationStage refreshTokenRotationStage;
    private final LogoutStage logoutStage;

    /**
     * Register Pipeline. Needs these params in context to work, then creates a user
     * 
     * @param email
     * @param password
     * @param props
     */
    public AuthPipeline createRegister() {
        AuthPipeline pipeline = new AuthPipeline()
                .add(validEmailStage)
                .add(validPasswordStage)
                .add(createAccountStage);
        if (authProperties.isEmailVerification()) {
            pipeline.add(emailVerificationStage);
        }
        return pipeline;
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
                .add(validEmailStage)
                .add(validPasswordStage)
                .add(authenticationStage);
        if (authProperties.isTwoFa()) {
            pipeline.add(setChallengeAndSend2FAstage);
        } else {
            pipeline.add(refreshTokenIssueStage)
                    .add(accessTokenIssueStage);

        }
        return pipeline;
    }

    public AuthPipeline createRefresh() {
        AuthPipeline pipeline = new AuthPipeline()
                .add(verifyRefreshTokenStage)
                .add(accessTokenIssueStage);
        if (authProperties.isRefreshTokenRotation()) {
            pipeline.add(refreshTokenRotationStage);
        }
        return pipeline;
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

    public AuthPipeline createLogout() {
        return new AuthPipeline()
                .add(logoutStage);
    }

}
