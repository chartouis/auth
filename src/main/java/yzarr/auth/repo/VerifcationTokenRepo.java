package yzarr.auth.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import yzarr.auth.model.VerificationToken;

public interface VerifcationTokenRepo extends JpaRepository<VerificationToken, UUID> {
    Optional<VerificationToken> findByTokenHash(String hash);
}
