package com.src.main.payment.service;

import java.util.Optional;

import com.src.main.payment.entity.PaymentConfigEntity;
import com.src.main.payment.enums.PaymentProviderType;

public interface PaymentConfigService {
	Optional<PaymentConfigEntity> getEnabledConfig(PaymentProviderType providerType);
	PaymentConfigEntity getDefaultEnabledConfig();
}
