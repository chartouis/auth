package yzarr.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import yzarr.auth.AuthProperties;
import yzarr.auth.model.RefreshToken;
import yzarr.auth.model.User;
import yzarr.auth.model.VerificationToken;
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
        return Base64.getEncoder().encodeToString(bytes);
    }

    private String hash(String token) {
        if (token == null)
            return null;
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            byte[] tokenHash = md.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(tokenHash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "ERROR";
    }

    /**
     * Generates a refresh token for authentication sessions.
     * Stores only a hashed version in DB.
     *
     * @param user       target user
     * @param rememberMe whether to use extended expiry
     * @return raw refresh token (returned once, never stored)
     */
    public String generateRefreshToken(User user, boolean rememberMe) {

        Instant expiresAt = Instant.now().plusMillis(
                rememberMe
                        ? props.getRefreshTokenExpiryMs()
                        : props.getShortAbsoluteExpiryMs());

        Instant absoluteExpiry = Instant.now().plusMillis(
                rememberMe
                        ? props.getAbsoluteExpiryMs()
                        : props.getShortAbsoluteExpiryMs());

        String tokenString = generateRandomString();
        String tokenHash = hash(tokenString);

        RefreshToken token = new RefreshToken(tokenHash, user, expiresAt, absoluteExpiry);
        refreshTokenRepo.save(token);

        return tokenString;
    }

    private String generateToken(User user,
            TokenType type,
            Instant expiresAt,
            String other) {

        String tokenString = generateRandomString();
        String tokenHash = hash(tokenString);

        VerificationToken token = new VerificationToken(tokenHash, user, expiresAt, type);
        token.setOther(other);

        verificationTokenRepo.save(token);
        return tokenString;
    }

    /**
     * Generates an email verification token used for account activation.
     * Stored as hash only.
     *
     * @param user target user
     * @return raw verification token
     */
    public String generateEmailVerificationToken(User user) {
        return generateToken(
                user,
                TokenType.EMAIL_VERIFICATION,
                Instant.now().plusMillis(props.getEmailVerificationTokenExpiryMs()),
                null);
    }

    /**
     * Generates a challenge token used in multi-step authentication flows.
     * Links to an email verification token via the "other" field.
     *
     * @param user       target user
     * @param emailToken related email verification token
     * @return raw challenge token
     */
    public String generateChallengeToken(User user, String emailToken) {
        return generateToken(
                user,
                TokenType.CHALLENGE,
                Instant.now().plusMillis(props.getChallengeTokenExpiryMs()),
                emailToken);
    }

    /**
     * Generates a second-factor authentication token.
     * Used for 2FA verification steps.
     *
     * @param user target user
     * @return raw 2FA token
     */
    public String generate2FAtoken(User user) {
        return generateToken(
                user,
                TokenType.TWO_FACTOR,
                Instant.now().plusMillis(props.getTwoFactorTokenExpiryMs()),
                null);
    }

    /**
     * returns Optional<User> object by token and type. Hashes it automatically
     * 
     * @param tokenString
     * @param type
     * @return optional of user
     */
    public Optional<User> getUserByToken(String tokenString, TokenType type) {
        switch (type) {
            case REFRESH_TOKEN:
                return getUserByTokenFromRefreshTokenRepo(tokenString);
            case TWO_FACTOR, EMAIL_VERIFICATION, CHALLENGE:
                return getUserByTokenFromVerificationTokenRepo(tokenString);
        }
        return Optional.empty();
    }

    private Optional<User> getUserByTokenFromRefreshTokenRepo(String tokenString) {
        Optional<RefreshToken> token = refreshTokenRepo.findByTokenHash(hash(tokenString));
        if (token.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(token.get().getUser());
    }

    private Optional<User> getUserByTokenFromVerificationTokenRepo(String tokenString) {
        Optional<VerificationToken> token = verificationTokenRepo.findByTokenHash(hash(tokenString));
        if (token.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(token.get().getUser());
    }

    public Optional<VerificationToken> getVerificationTokenByOther(String other) {
        return verificationTokenRepo.findByOther(other);
    }

    public Optional<VerificationToken> getVerificationTokenByTokenHash(String tokenString) {
        return verificationTokenRepo.findByTokenHash(hash(tokenString));
    }
}
