package com.hunghv.inmobitestjava.entity;

import com.hunghv.inmobitestjava.exception.BadRequestException;
import com.hunghv.inmobitestjava.utils.EmailUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
    name = "users",
    uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
    indexes = @Index(name = "idx_users_score_id", columnList = "score DESC, id ASC")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private int turns;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public UserAccount(String email, String passwordHash) {
        this.email = EmailUtils.normalizeEmail(email);
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash must not be null");
        this.score = 0;
        this.turns = 0;
        this.version = 0L;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.version == null) {
            this.version = 0L;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void addTurns(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        this.turns = Math.addExact(this.turns, amount);
    }

    public void consumeTurn() {
        if (this.turns <= 0) {
            throw new BadRequestException("Not enough turns to make a guess");
        }
        this.turns -= 1;
    }

    public void increaseScore() {
        this.score = Math.addExact(this.score, 1);
    }

    public void updatePassword(String newPasswordHash) {
        this.passwordHash = Objects.requireNonNull(newPasswordHash, "newPasswordHash must not be null");
    }
}
