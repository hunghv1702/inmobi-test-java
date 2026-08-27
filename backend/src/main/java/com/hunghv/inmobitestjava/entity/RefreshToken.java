package com.hunghv.inmobitestjava.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

@Getter
@Entity
@Table(
    name = "refresh_tokens",
    uniqueConstraints = @UniqueConstraint(name = "uk_refresh_tokens_token", columnNames = "token")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    @Column(nullable = false)
    private Long version;

    public RefreshToken(UserAccount user, String token, Instant expiryDate) {
        this.user = Objects.requireNonNull(user, "user must not be null");
        this.token = Objects.requireNonNull(token, "token must not be null");
        this.expiryDate = Objects.requireNonNull(expiryDate, "expiryDate must not be null");
        this.version = 0L;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        if (this.version == null) {
            this.version = 0L;
        }
    }

    public boolean isExpired() {
        return expiryDate.isBefore(Instant.now());
    }
}
