package yzarr.auth.model;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import yzarr.auth.model.enums.RefreshTokenStatus;
import yzarr.auth.model.enums.RevokeReason;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, updatable = false, length = 255)
    private String tokenHash;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant issuedAt;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private Instant absoluteExpiry;

    @Column(nullable = true)
    private Instant revokedAt;

    @Column(nullable = false)
    private boolean rememberMe = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefreshTokenStatus status = RefreshTokenStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 20)
    private RevokeReason revokeReason;

    public RefreshToken(
            String tokenHash,
            User user,
            Instant expiresAt,
            Instant absoluteExpiry) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.absoluteExpiry = absoluteExpiry;
    }

    public void revoke(RevokeReason reason) {
        this.status = RefreshTokenStatus.REVOKED;
        this.revokeReason = reason;
        this.revokedAt = Instant.now();
    }
}