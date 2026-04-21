package com.src.main.payment.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.payment.entity.PaymentEntity;
import com.src.main.payment.enums.PaymentStatus;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
	List<PaymentEntity> findTop100ByStatusInAndNextPollAtBeforeOrderByNextPollAtAsc(List<PaymentStatus> statuses, Instant nextPollAt);
	java.util.Optional<PaymentEntity> findFirstByProviderReference(String providerReference);
}
