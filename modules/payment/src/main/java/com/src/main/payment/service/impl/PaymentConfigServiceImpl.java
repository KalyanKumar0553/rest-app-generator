package com.src.main.payment.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.payment.entity.PaymentConfigEntity;
import com.src.main.payment.enums.PaymentProviderType;
import com.src.main.payment.exception.PaymentConfigurationException;
import com.src.main.payment.exception.PaymentErrorCode;
import com.src.main.payment.repository.PaymentConfigRepository;
import com.src.main.payment.service.PaymentConfigService;

@Service
public class PaymentConfigServiceImpl implements PaymentConfigService {
	private final PaymentConfigRepository repository;

	public PaymentConfigServiceImpl(PaymentConfigRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<PaymentConfigEntity> getEnabledConfig(PaymentProviderType providerType) {
		return repository.findFirstByEnabledTrueAndProviderTypeOrderByDefaultProviderDescCreatedAtAsc(providerType);
	}

	@Override
	@Transactional(readOnly = true)
	public PaymentConfigEntity getDefaultEnabledConfig() {
		return repository.findFirstByEnabledTrueAndDefaultProviderTrue()
				.orElseThrow(() -> new PaymentConfigurationException(PaymentErrorCode.CONFIG_NOT_FOUND, "No enabled default payment provider configured."));
	}
}
