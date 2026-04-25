package yzarr.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import yzarr.auth.AuthProperties;
import yzarr.auth.model.AuthException;
import yzarr.auth.model.RefreshToken;
import yzarr.auth.model.TokenException;
import yzarr.auth.model.User;
import yzarr.auth.model.VerificationToken;
import yzarr.auth.model.enums.ErrorCode;
import yzarr.auth.model.enums.Status;
import yzarr.auth.model.enums.TokenFailureReason;
import yzarr.auth.model.enums.TokenType;
import yzarr.auth.repo.RefreshTokenRepo;
import yzarr.auth.repo.VerificationTokenRepo;

@Service
@Slf4j
public class TokenService {
    private final RefreshTokenRepo refreshTokenRepo;
    private final VerificationTokenRepo verificationTokenRepo;
    private final SecureRandom random;
    private final AuthProperties props;

    public TokenService(RefreshTokenRepo refreshTokenRepo, VerificationTokenRepo verificationTokenRepo,
            AuthProperties props) {
        this.refreshTokenRepo = refreshTokenRepo;
        this.verificationTokenRepo = verificationTokenRepo;
        this.random = new SecureRandom();
        this.props = props;
    }

    private String generateRandomString() {
        byte[] bytes = new byte[40];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        if (token == null)
            return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] tokenHash = md.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(tokenHash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Fetches a VerificationToken by raw token string, validates expiry and status.
     * Sets status to EXPIRED in DB if past expiresAt.
     *
     * @throws TokenException MISSING | EXPIRED
     */
    public VerificationToken findValidVerificationToken(String tokenString, TokenType type) {
        if (tokenString == null) {
            throw new TokenException(type, TokenFailureReason.MISSING);
        }
        VerificationToken token = verificationTokenRepo
                .findByTokenHash(hash(tokenString))
                .orElseThrow(() -> new TokenException(type, TokenFailureReason.INVALID));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            token.setStatus(Status.EXPIRED);
            verificationTokenRepo.save(token);
            throw new TokenException(type, TokenFailureReason.EXPIRED);
        }

        if (token.getType() != type) {
            throw new TokenException(type, TokenFailureReason.INVALID);
        }

        if (token.getStatus() == Status.CONSUMED) {
            throw new TokenException(type, TokenFailureReason.ALREADY_CONSUMED);
        }

        return token;
    }

    /**
     * Fetches a RefreshToken by raw token string, validates expiry.
     * Sets status to EXPIRED in DB if past absoluteExpiry.
     *
     * @throws TokenException MISSING | EXPIRED
     */
    public RefreshToken findValidRefreshToken(String tokenString) {
        if (tokenString == null) {
            throw new TokenException(TokenType.REFRESH_TOKEN, TokenFailureReason.MISSING);
        }
        RefreshToken token = refreshTokenRepo
                .findByTokenHash(hash(tokenString))
                .orElseThrow(() -> new TokenException(TokenType.REFRESH_TOKEN, TokenFailureReason.MISSING));

        if (token.getAbsoluteExpiry().isBefore(Instant.now()) || token.getExpiresAt().isBefore(Instant.now())) {
            token.setStatus(Status.EXPIRED);
            refreshTokenRepo.save(token);
            throw new TokenException(TokenType.REFRESH_TOKEN, TokenFailureReason.EXPIRED);
        }

        return token;
    }

    // -------------------------------------------------------------------------
    // Generation
    // -------------------------------------------------------------------------

    public String generateRefreshToken(User user, boolean rememberMe) {
        Instant expiresAt = Instant.now().plusMillis(
                rememberMe ? props.getRefreshTokenExpiryMs() : props.getShortAbsoluteExpiryMs());
        Instant absoluteExpiry = Instant.now().plusMillis(
                rememberMe ? props.getAbsoluteExpiryMs() : props.getShortAbsoluteExpiryMs());

        String tokenString = generateRandomString();
        refreshTokenRepo.save(new RefreshToken(hash(tokenString), user, expiresAt, absoluteExpiry));
        return tokenString;
    }

    private String generateToken(User user, TokenType type, Instant expiresAt, String other) {
        String tokenString = generateRandomString();
        VerificationToken token = new VerificationToken(hash(tokenString), user, expiresAt, type);
        token.setOther(other);
        verificationTokenRepo.save(token);
        return tokenString;
    }

    public String generateEmailVerificationToken(User user) {
        return generateToken(user, TokenType.EMAIL_VERIFICATION,
                Instant.now().plusMillis(props.getEmailVerificationTokenExpiryMs()), null);
    }

    public String generateChallengeToken(User user, String emailToken) {
        return generateToken(user, TokenType.CHALLENGE,
                Instant.now().plusMillis(props.getChallengeTokenExpiryMs()), emailToken);
    }

    public String generate2FAtoken(User user) {
        return generateToken(user, TokenType.TWO_FACTOR,
                Instant.now().plusMillis(props.getTwoFactorTokenExpiryMs()), null);
    }

    public User getUserByToken(String tokenString, TokenType type) {
        return switch (type) {
            case REFRESH_TOKEN -> findValidRefreshToken(tokenString).getUser();
            case TWO_FACTOR, EMAIL_VERIFICATION, CHALLENGE -> findValidVerificationToken(tokenString, type).getUser();
            default -> throw new AuthException(ErrorCode.UNEXPECTED_ERROR);
        };
    }

    // REFACTOR
    public VerificationToken getVerificationTokenByOther(String other, TokenType type) {
        if (other == null) {
            throw new TokenException(type, TokenFailureReason.MISSING);
        }
        VerificationToken token = verificationTokenRepo
                .findByOther(other)
                .orElseThrow(() -> new TokenException(type, TokenFailureReason.INVALID));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            token.setStatus(Status.EXPIRED);
            verificationTokenRepo.save(token);
            throw new TokenException(type, TokenFailureReason.EXPIRED);
        }

        if (token.getType() != type) {
            throw new TokenException(type, TokenFailureReason.INVALID);
        }

        if (token.getStatus() == Status.CONSUMED) {
            throw new TokenException(type, TokenFailureReason.ALREADY_CONSUMED);
        }

        return token;
    }

    public VerificationToken save(VerificationToken vt) {
        return verificationTokenRepo.save(vt);
    }

    public RefreshToken save(RefreshToken rt) {
        return refreshTokenRepo.save(rt);
    }

}