package com.src.main.payment.service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.src.main.payment.enums.PaymentProviderType;
import com.src.main.payment.exception.PaymentErrorCode;
import com.src.main.payment.exception.PaymentConfigurationException;

@Component
public class PaymentGatewayRegistry {
	private final Map<PaymentProviderType, PaymentGatewayClient> clients = new EnumMap<>(PaymentProviderType.class);

	public PaymentGatewayRegistry(List<PaymentGatewayClient> clientList) {
		for (PaymentGatewayClient client : clientList) {
			clients.put(client.providerType(), client);
		}
	}

	public PaymentGatewayClient resolve(PaymentProviderType providerType) {
		PaymentGatewayClient client = clients.get(providerType);
		if (client == null) {
			throw new PaymentConfigurationException(PaymentErrorCode.PROVIDER_UNAVAILABLE, "No payment gateway client registered for " + providerType);
		}
		return client;
	}
}
