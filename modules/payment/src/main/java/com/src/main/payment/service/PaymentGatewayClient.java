package com.src.main.payment.service;

import com.src.main.payment.dto.CreateOrderRequest;
import com.src.main.payment.enums.PaymentProviderType;

public interface PaymentGatewayClient {
	PaymentProviderType providerType();
	PaymentCreateResult createPayment(String orderReference, CreateOrderRequest request);
	PaymentStatusResult fetchStatus(String paymentReference);

	record PaymentCreateResult(String paymentReference, String providerOrderId, String paymentRedirectUrl, String rawResponseJson) {}
	record PaymentStatusResult(String status, String reason, String rawResponseJson, String providerPaymentId) {}
}
