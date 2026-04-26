package yzarr.auth.model;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
import yzarr.auth.model.enums.VerificationTokenStatus;
import yzarr.auth.model.enums.TokenType;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "verification_tokens")
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, unique = true)
    private UUID id;

    @Column(nullable = false, unique = true, updatable = false, length = 255)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Instant issuedAt;

    @Column(updatable = false, nullable = false)
    private Instant expiresAt;

    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TokenType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VerificationTokenStatus status = VerificationTokenStatus.PENDING;

    @Column(length = 255, nullable = true)
    private String metadata;

    @UpdateTimestamp
    private Instant changedAt;

    public VerificationToken(String tokenHash, User user, Instant expiresAt, TokenType type) {
        this.tokenHash = tokenHash;
        this.user = user;
        this.expiresAt = expiresAt;
        this.type = type;
    }

}
