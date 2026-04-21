package com.src.main.payment.service;

import java.time.Duration;

import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.src.main.payment.exception.PaymentErrorCode;
import com.src.main.payment.exception.PaymentProviderException;

public abstract class AbstractHttpPaymentGatewayClient {
	protected final WebClient webClient;
	protected final ObjectMapper objectMapper;

	protected AbstractHttpPaymentGatewayClient(String baseUrl, ObjectMapper objectMapper) {
		this.webClient = WebClient.builder().baseUrl(baseUrl == null ? "" : baseUrl.trim()).build();
		this.objectMapper = objectMapper;
	}

	protected void requireConfigured(String baseUrl) {
		if (baseUrl == null || baseUrl.isBlank()) {
			throw new PaymentProviderException(PaymentErrorCode.PROVIDER_UNAVAILABLE, "Payment gateway is not configured.");
		}
	}

	protected String toJson(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (Exception ex) {
			throw new PaymentProviderException(PaymentErrorCode.GATEWAY_FAILURE, "Failed to serialize payment payload", true, ex);
		}
	}

	protected <T> T fromJson(String value, Class<T> type) {
		try {
			return objectMapper.readValue(value, type);
		} catch (Exception ex) {
			throw new PaymentProviderException(PaymentErrorCode.GATEWAY_FAILURE, "Failed to parse payment response", true, ex);
		}
	}

	protected Duration timeout() {
		return Duration.ofSeconds(15);
	}
}
