package com.src.main.payment.service.impl;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.payment.dto.PaymentConfigRequest;
import com.src.main.payment.dto.PaymentConfigResponse;
import com.src.main.payment.entity.PaymentConfigEntity;
import com.src.main.payment.exception.PaymentConfigurationException;
import com.src.main.payment.exception.PaymentErrorCode;
import com.src.main.payment.exception.PaymentValidationException;
import com.src.main.payment.repository.PaymentConfigRepository;
import com.src.main.payment.service.PaymentConfigAdminService;

@Service
public class PaymentConfigAdminServiceImpl implements PaymentConfigAdminService {
	private final PaymentConfigRepository repository;
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
	private final SecureRandom secureRandom = new SecureRandom();

	public PaymentConfigAdminServiceImpl(PaymentConfigRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional
	public PaymentConfigResponse save(PaymentConfigRequest request) {
		if (request.getProviderType() == null) {
			throw new PaymentValidationException(PaymentErrorCode.CONFIG_INVALID, "Payment provider type is required.");
		}
		PaymentConfigEntity entity = repository.findFirstByProviderType(request.getProviderType())
				.orElseGet(PaymentConfigEntity::new);
		entity.setProviderType(request.getProviderType());
		entity.setEnabled(request.isEnabled());
		entity.setDefaultProvider(request.isDefaultProvider());
		if (entity.isDefaultProvider() && !entity.isEnabled()) {
			throw new PaymentConfigurationException(PaymentErrorCode.CONFIG_INVALID, "A default payment provider must be enabled.");
		}
		entity.setMerchantId(normalize(request.getMerchantId()));
		entity.setPublicKey(normalize(request.getPublicKey()));
		entity.setEndpointUrl(normalize(request.getEndpointUrl()));
		if (request.getSecretKey() != null && !request.getSecretKey().isBlank()) {
			String salt = randomSalt();
			entity.setSecretKeySalt(salt);
			entity.setSecretKeyHash(passwordEncoder.encode(salt + ":" + request.getSecretKey().trim()));
			entity.setSecretKeyEncrypted(encryptPlaceholder(request.getSecretKey().trim(), salt));
		}
		if (request.getWebhookSecret() != null && !request.getWebhookSecret().isBlank()) {
			String salt = randomSalt();
			entity.setWebhookSecretSalt(salt);
			entity.setWebhookSecretHash(passwordEncoder.encode(salt + ":" + request.getWebhookSecret().trim()));
			entity.setWebhookSecretEncrypted(encryptPlaceholder(request.getWebhookSecret().trim(), salt));
		}
		return toResponse(repository.save(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public List<PaymentConfigResponse> findAll() {
		return repository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
	}

	private PaymentConfigResponse toResponse(PaymentConfigEntity entity) {
		return new PaymentConfigResponse(entity.getId(), entity.getProviderType(), entity.isEnabled(), entity.isDefaultProvider(), entity.getMerchantId(), entity.getPublicKey(), entity.getEndpointUrl(), entity.getCreatedAt(), entity.getUpdatedAt());
	}

	private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
	private String randomSalt() { byte[] salt = new byte[16]; secureRandom.nextBytes(salt); return Base64.getEncoder().encodeToString(salt); }
	private String encryptPlaceholder(String value, String salt) { return "enc::" + Base64.getEncoder().encodeToString((salt + ":" + value).getBytes(java.nio.charset.StandardCharsets.UTF_8)); }
}
