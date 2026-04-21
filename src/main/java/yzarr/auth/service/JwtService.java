package yzarr.auth.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import yzarr.auth.AuthProperties;

@Slf4j
@Service
public class JwtService {

    private final AuthProperties props;

    public JwtService(AuthProperties props) {
        this.props = props;
        log.info("JwtService initialized");
    }

    /**
     * Generates a JWT as a String. The subject should be UUID of the user
     * It can be email, but not recommended. Username CAN'T be the subject, because
     * it can be changed easily.
     * 
     * @param subject
     * @return jwt
     */
    public String generateToken(String subject) {
        Map<String, Object> claims = new HashMap<>();
        String token = Jwts.builder()
                .claims()
                .add(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + props.getAccessTokenExpiryMs()))
                .and()
                .signWith(getKey())
                .compact();
        log.info("Generated JWT for subject: {}", subject);
        return token;
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(props.getJwtSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extracts the subject from the token (email or user ID, depending on what was
     * passed to generateToken).
     */
    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Accepts token, and then compares the expectedSubject with the subject of the
     * token
     * If they are equal returns true, otherwise false
     * 
     * @param jwt
     * @param expectedSubject
     * @return true or false
     */
    public boolean validateToken(String token, String expectedSubject) {
        try {
            String subject = extractSubject(token);
            boolean valid = subject.equals(expectedSubject) && !isTokenExpired(token);
            if (!valid) {
                log.warn("Token validation failed — subject mismatch or expired for: {}", expectedSubject);
            }
            return valid;
        } catch (ExpiredJwtException e) {
            log.warn("Token has expired: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}