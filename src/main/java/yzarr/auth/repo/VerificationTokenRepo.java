package yzarr.auth.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import yzarr.auth.model.VerificationToken;

public interface VerificationTokenRepo extends JpaRepository<VerificationToken, UUID> {
    Optional<VerificationToken> findByTokenHash(String hash);

    Optional<VerificationToken> findByMetadata(String metadata);
}
