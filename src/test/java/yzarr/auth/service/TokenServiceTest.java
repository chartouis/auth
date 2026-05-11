package yzarr.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import yzarr.auth.AuthProperties;
import yzarr.auth.model.RefreshToken;
import yzarr.auth.model.TokenException;
import yzarr.auth.model.User;
import yzarr.auth.model.VerificationToken;
import yzarr.auth.model.enums.RefreshTokenStatus;
import yzarr.auth.model.enums.RevokeReason;
import yzarr.auth.model.enums.TokenFailureReason;
import yzarr.auth.model.enums.TokenType;
import yzarr.auth.model.enums.VerificationTokenStatus;
import yzarr.auth.repo.RefreshTokenRepo;
import yzarr.auth.repo.VerificationTokenRepo;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    RefreshTokenRepo refreshTokenRepo;

    @Mock
    VerificationTokenRepo verificationTokenRepo;

    AuthProperties props;

    @InjectMocks
    TokenService tokenService;

    @BeforeEach
    void setUp() {
        props = new AuthProperties();
        props.setRefreshTokenExpiryMs(1000L * 60L * 60L * 24L * 30L);
        props.setAbsoluteExpiryMs(1000L * 60L * 60L * 24L * 90L);
        props.setShortAbsoluteExpiryMs(1000L * 60L * 60L);
        props.setEmailVerificationTokenExpiryMs(1000L * 60L * 60L);
        props.setPasswordResetTokenExpiryMs(1000L * 60L * 5L);
        props.setChallengeTokenExpiryMs(1000L * 600L);
        props.setTwoFactorTokenExpiryMs(1000L * 600L);

        tokenService = new TokenService(refreshTokenRepo, verificationTokenRepo, props);
    }

    @Test
    void generateRefreshToken_returnsNonNullNonEmpty_andSaves() {
        User user = new User("a@b.com", "hash");

        String token = tokenService.generateRefreshToken(user, true);

        assertThat(token).isNotNull().isNotEmpty();
        verify(refreshTokenRepo).save(any(RefreshToken.class));
    }

    @Test
    void generateRefreshToken_rememberMe_true_vs_false_expiresAtDiffers() {
        User user = new User("a@b.com", "hash");

        ArgumentCaptor<RefreshToken> cap = ArgumentCaptor.forClass(RefreshToken.class);

        tokenService.generateRefreshToken(user, true);
        verify(refreshTokenRepo).save(cap.capture());
        Instant rememberMeExpiresAt = cap.getValue().getExpiresAt();

        org.mockito.Mockito.reset(refreshTokenRepo);
        tokenService.generateRefreshToken(user, false);
        verify(refreshTokenRepo).save(cap.capture());
        Instant shortExpiresAt = cap.getValue().getExpiresAt();

        assertThat(rememberMeExpiresAt).isAfter(shortExpiresAt);
    }

    @Test
    void findValidRefreshToken_missing_whenTokenStringNull() {
        assertThatThrownBy(() -> tokenService.findValidRefreshToken(null))
                .isInstanceOf(TokenException.class)
                .extracting(e -> ((TokenException) e).getReason())
                .isEqualTo(TokenFailureReason.MISSING);
    }

    @Test
    void findValidRefreshToken_missing_whenHashNotFound() {
        when(refreshTokenRepo.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tokenService.findValidRefreshToken("raw"))
                .isInstanceOf(TokenException.class)
                .extracting(e -> ((TokenException) e).getReason())
                .isEqualTo(TokenFailureReason.MISSING);
    }

    @Test
    void findValidRefreshToken_expired_whenAbsoluteExpiryPast_savesRevoked() {
        RefreshToken rt = new RefreshToken("hash", new User("a@b.com", "hash"),
                Instant.now().plusSeconds(60), Instant.now().minusSeconds(1));
        when(refreshTokenRepo.findByTokenHash(any())).thenReturn(Optional.of(rt));

        assertThatThrownBy(() -> tokenService.findValidRefreshToken("raw"))
                .isInstanceOf(TokenException.class)
                .extracting(e -> ((TokenException) e).getReason())
                .isEqualTo(TokenFailureReason.EXPIRED);

        assertThat(rt.getStatus()).isEqualTo(RefreshTokenStatus.REVOKED);
        assertThat(rt.getRevokeReason()).isEqualTo(RevokeReason.EXPIRED);
        verify(refreshTokenRepo).save(eq(rt));
    }

    @Test
    void findValidRefreshToken_expired_whenExpiresAtPast_savesRevoked() {
        RefreshToken rt = new RefreshToken("hash", new User("a@b.com", "hash"),
                Instant.now().minusSeconds(1), Instant.now().plusSeconds(60));
        when(refreshTokenRepo.findByTokenHash(any())).thenReturn(Optional.of(rt));

        assertThatThrownBy(() -> tokenService.findValidRefreshToken("raw"))
                .isInstanceOf(TokenException.class)
                .extracting(e -> ((TokenException) e).getReason())
                .isEqualTo(TokenFailureReason.EXPIRED);

        assertThat(rt.getStatus()).isEqualTo(RefreshTokenStatus.REVOKED);
        verify(refreshTokenRepo).save(eq(rt));
    }

    @Test
    void findValidRefreshToken_alreadyRevoked_whenStatusRevoked() {
        RefreshToken rt = new RefreshToken("hash", new User("a@b.com", "hash"),
                Instant.now().plusSeconds(60), Instant.now().plusSeconds(120));
        rt.setStatus(RefreshTokenStatus.REVOKED);
        when(refreshTokenRepo.findByTokenHash(any())).thenReturn(Optional.of(rt));

        assertThatThrownBy(() -> tokenService.findValidRefreshToken("raw"))
                .isInstanceOf(TokenException.class)
                .extracting(e -> ((TokenException) e).getReason())
                .isEqualTo(TokenFailureReason.ALREADY_REVOKED);

        verify(refreshTokenRepo, never()).save(any());
    }

    @Test
    void findValidRefreshToken_returnsToken_whenValid() {
        RefreshToken rt = new RefreshToken("hash", new User("a@b.com", "hash"),
                Instant.now().plusSeconds(60), Instant.now().plusSeconds(120));
        when(refreshTokenRepo.findByTokenHash(any())).thenReturn(Optional.of(rt));

        RefreshToken out = tokenService.findValidRefreshToken("raw");

        assertThat(out).isSameAs(rt);
    }

    @Test
    void findValidVerificationToken_missing_whenTokenStringNull() {
        assertThatThrownBy(() -> tokenService.findValidVerificationToken(null, TokenType.EMAIL_VERIFICATION))
                .isInstanceOf(TokenException.class)
                .extracting(e -> ((TokenException) e).getReason())
                .isEqualTo(TokenFailureReason.MISSING);
    }

    @Test
    void findValidVerificationToken_invalid_whenHashNotFound() {
        when(verificationTokenRepo.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tokenService.findValidVerificationToken("raw", TokenType.EMAIL_VERIFICATION))
                .isInstanceOf(TokenException.class)
                .extracting(e -> ((TokenException) e).getReason())
                .isEqualTo(TokenFailureReason.INVALID);
    }

    @Test
    void findValidVerificationToken_expired_whenExpiresAtPast() {
        VerificationToken vt = new VerificationToken("hash", new User("a@b.com", "hash"),
                Instant.now().minusSeconds(1), TokenType.EMAIL_VERIFICATION);
        when(verificationTokenRepo.findByTokenHash(any())).thenReturn(Optional.of(vt));

        assertThatThrownBy(() -> tokenService.findValidVerificationToken("raw", TokenType.EMAIL_VERIFICATION, true))
                .isInstanceOf(TokenException.class)
                .extracting(e -> ((TokenException) e).getReason())
                .isEqualTo(TokenFailureReason.EXPIRED);
    }

    @Test
    void findValidVerificationToken_alreadyConsumed_whenStatusConsumed() {
        VerificationToken vt = new VerificationToken("hash", new User("a@b.com", "hash"),
                Instant.now().plusSeconds(60), TokenType.EMAIL_VERIFICATION);
        vt.setStatus(VerificationTokenStatus.CONSUMED);
        when(verificationTokenRepo.findByTokenHash(any())).thenReturn(Optional.of(vt));

        assertThatThrownBy(() -> tokenService.findValidVerificationToken("raw", TokenType.EMAIL_VERIFICATION, true))
                .isInstanceOf(TokenException.class)
                .extracting(e -> ((TokenException) e).getReason())
                .isEqualTo(TokenFailureReason.ALREADY_CONSUMED);
    }

    @Test
    void findValidVerificationToken_invalid_whenTypeMismatch() {
        VerificationToken vt = new VerificationToken("hash", new User("a@b.com", "hash"),
                Instant.now().plusSeconds(60), TokenType.PASSWORD_RESET);
        when(verificationTokenRepo.findByTokenHash(any())).thenReturn(Optional.of(vt));

        assertThatThrownBy(() -> tokenService.findValidVerificationToken("raw", TokenType.EMAIL_VERIFICATION, true))
                .isInstanceOf(TokenException.class)
                .extracting(e -> ((TokenException) e).getReason())
                .isEqualTo(TokenFailureReason.INVALID);
    }

    @Test
    void findValidVerificationToken_returnsToken_whenValid() {
        VerificationToken vt = new VerificationToken("hash", new User("a@b.com", "hash"),
                Instant.now().plusSeconds(60), TokenType.EMAIL_VERIFICATION);
        when(verificationTokenRepo.findByTokenHash(any())).thenReturn(Optional.of(vt));

        VerificationToken out = tokenService.findValidVerificationToken("hash", TokenType.EMAIL_VERIFICATION, true);

        assertThat(out).isSameAs(vt);
    }

    @Test
    void generate2FAtoken_metadataIsHashedChallenge_notRaw() {
        User user = new User("a@b.com", "hash");
        when(verificationTokenRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String rawChallenge = "my-challenge";
        tokenService.generate2FAtoken(user, rawChallenge);

        ArgumentCaptor<VerificationToken> cap = ArgumentCaptor.forClass(VerificationToken.class);
        verify(verificationTokenRepo).save(cap.capture());
        assertThat(cap.getValue().getMetadata()).isNotEqualTo(rawChallenge);
        assertThat(cap.getValue().getMetadata()).isNotBlank();
    }

    @Test
    void generateChallengeToken_metadataEqualsBooleanString() {
        User user = new User("a@b.com", "hash");
        when(verificationTokenRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        tokenService.generateChallengeToken(user, true);
        ArgumentCaptor<VerificationToken> cap = ArgumentCaptor.forClass(VerificationToken.class);
        verify(verificationTokenRepo).save(cap.capture());
        assertThat(cap.getValue().getMetadata()).isEqualTo(Boolean.toString(true));
    }
}

