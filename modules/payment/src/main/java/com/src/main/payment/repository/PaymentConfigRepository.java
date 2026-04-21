package com.src.main.payment.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.payment.entity.PaymentConfigEntity;
import com.src.main.payment.enums.PaymentProviderType;

public interface PaymentConfigRepository extends JpaRepository<PaymentConfigEntity, UUID> {
	Optional<PaymentConfigEntity> findFirstByProviderType(PaymentProviderType providerType);
	Optional<PaymentConfigEntity> findFirstByEnabledTrueAndProviderTypeOrderByDefaultProviderDescCreatedAtAsc(PaymentProviderType providerType);
	Optional<PaymentConfigEntity> findFirstByEnabledTrueAndDefaultProviderTrue();
}
