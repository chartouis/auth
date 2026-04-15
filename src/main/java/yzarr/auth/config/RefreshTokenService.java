package yzarr.auth.config;

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
import yzarr.auth.RefreshTokenRepo;
import yzarr.auth.model.RefreshToken;
import yzarr.auth.model.User;

@Service
@Slf4j
public class RefreshTokenService {
    private final RefreshTokenRepo repo;
    private final SecureRandom random;
    private final AuthProperties props;

    public RefreshTokenService(RefreshTokenRepo repo, AuthProperties props) {
        this.repo = repo;
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
    public String generate(User user, boolean rememberMe) {
        String tokenString = generateRandomString();
        String tokenHash = hash(tokenString);

        Instant expiresAt = rememberMe ? Instant.now().plusMillis(props.getRefreshTokenExpiryMs())
                : Instant.now().plusMillis(props.getShortAbsoluteExpiryMs());

        Instant absoluteExpiry = rememberMe ? Instant.now().plusMillis(props.getAbsoluteExpiryMs())
                : Instant.now().plusMillis(props.getShortAbsoluteExpiryMs());

        RefreshToken token = new RefreshToken(tokenHash, user, expiresAt, absoluteExpiry);
        repo.save(token);
        return tokenString;
    }

    /**
     * returns Optional<User> object by refresh token. Hashes it automatically
     * 
     * @param tokenString
     * @return optional of user
     */
    public Optional<User> getUserByToken(String tokenString) {
        Optional<RefreshToken> token = repo.findByTokenHash(hash(tokenString));
        if (token.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(token.get().getUser());
    }

}
