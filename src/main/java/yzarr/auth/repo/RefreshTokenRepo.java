package yzarr.auth.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import yzarr.auth.model.RefreshToken;
import yzarr.auth.model.User;

import java.util.List;

@Repository
public interface RefreshTokenRepo extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String hash);

    List<RefreshToken> findByUser(User user);
}
