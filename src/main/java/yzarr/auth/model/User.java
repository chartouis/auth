package yzarr.auth.model;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, unique = true)
    private String email;

    @CreatedDate
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    private Role role = Role.USER;

    private boolean isActive = true;

    public User(String email, String password) {
        this.email = email;
        this.passwordHash = password;
        this.username = email.split("@")[0];
    }

    public User(String email, String password, String username) {
        this.email = email;
        this.passwordHash = password;
        this.username = username;
    }
}
