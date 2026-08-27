package com.hunghv.inmobitestjava.entity;

import com.hunghv.inmobitestjava.constant.PaymentStatus;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@Table(
    name = "payment_transactions",
    uniqueConstraints = @UniqueConstraint(name = "uk_payment_transactions_provider_session", columnNames = "provider_session_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(nullable = false, length = 32)
    private String provider;

    @Column(name = "provider_session_id", nullable = false, length = 255)
    private String providerSessionId;

    @Column(name = "checkout_url", nullable = false, length = 2048)
    private String checkoutUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status;

    @Column(nullable = false)
    private int turns;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public PaymentTransaction(
        UserAccount user,
        String provider,
        String providerSessionId,
        String checkoutUrl,
        PaymentStatus status,
        int turns,
        long amount,
        String currency
    ) {
        this.user = user;
        this.provider = provider;
        this.providerSessionId = providerSessionId;
        this.checkoutUrl = checkoutUrl;
        this.status = status;
        this.turns = turns;
        this.amount = amount;
        this.currency = currency;
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

    public boolean belongsTo(Long userId) {
        return user.getId().equals(userId);
    }

    public boolean isPaid() {
        return PaymentStatus.PAID.equals(status);
    }

    public void markPaid() {
        this.status = PaymentStatus.PAID;
        this.paidAt = Instant.now();
    }

    public void markFailed() {
        this.status = PaymentStatus.FAILED;
    }

    public void markExpired() {
        this.status = PaymentStatus.EXPIRED;
    }
}
