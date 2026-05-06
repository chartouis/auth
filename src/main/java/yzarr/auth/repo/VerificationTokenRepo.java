package yzarr.auth.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import yzarr.auth.model.User;
import yzarr.auth.model.VerificationToken;
import yzarr.auth.model.enums.TokenType;

public interface VerificationTokenRepo extends JpaRepository<VerificationToken, UUID> {
    Optional<VerificationToken> findByTokenHash(String hash);

    Optional<VerificationToken> findByMetadata(String metadata);

    Optional<VerificationToken> findTopByUserAndTypeOrderByIssuedAtDesc(User user, TokenType type);

}
