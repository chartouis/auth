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
import yzarr.auth.repo.VerifcationTokenRepo;

@Service
@Slf4j
public class TokenService {
    private final RefreshTokenRepo refreshTokenRepo;
    private final VerifcationTokenRepo verifcationTokenRepo;
    private final SecureRandom random;
    private final AuthProperties props;

    public TokenService(RefreshTokenRepo refreshTokenRepo, VerifcationTokenRepo verifcationTokenRepo,
            AuthProperties props) {
        this.refreshTokenRepo = refreshTokenRepo;
        this.verifcationTokenRepo = verifcationTokenRepo;
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
     * Generates a token for the given user.
     * Returns the actual Refresh Token, and saves its hash to db
     * 
     * @return refresh token
     */
    public String generateRefreshToken(User user, boolean rememberMe) {
        String tokenString = generateRandomString();
        String tokenHash = hash(tokenString);

        Instant expiresAt = rememberMe ? Instant.now().plusMillis(props.getRefreshTokenExpiryMs())
                : Instant.now().plusMillis(props.getShortAbsoluteExpiryMs());

        Instant absoluteExpiry = rememberMe ? Instant.now().plusMillis(props.getAbsoluteExpiryMs())
                : Instant.now().plusMillis(props.getShortAbsoluteExpiryMs());

        RefreshToken token = new RefreshToken(tokenHash, user, expiresAt, absoluteExpiry);
        refreshTokenRepo.save(token);
        return tokenString;
    }

    /**
     * Generates a token for the given user.
     * Returns the actual Verification Token, and saves its hash to db
     * 
     * @return verification token
     */
    public String generateEmailVerificationToken(User user) {
        String tokenString = generateRandomString();
        String tokenHash = hash(tokenString);
        Instant expiresAt = Instant.now().plusMillis(props.getEmailVerificationTokenExpiryMs());
        VerificationToken token = new VerificationToken(tokenHash, user, expiresAt, TokenType.EMAIL_VERIFICATION);
        verifcationTokenRepo.save(token);
        return tokenString;
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
            case TWO_FACTOR, EMAIL_VERIFICATION:
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
        Optional<VerificationToken> token = verifcationTokenRepo.findByTokenHash(hash(tokenString));
        if (token.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(token.get().getUser());
    }

}
