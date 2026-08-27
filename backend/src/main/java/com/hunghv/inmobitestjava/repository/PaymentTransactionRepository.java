package com.hunghv.inmobitestjava.repository;

import com.hunghv.inmobitestjava.entity.PaymentTransaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PaymentTransaction p join fetch p.user where p.providerSessionId = :providerSessionId")
    Optional<PaymentTransaction> findByProviderSessionIdForUpdate(@Param("providerSessionId") String providerSessionId);
}
