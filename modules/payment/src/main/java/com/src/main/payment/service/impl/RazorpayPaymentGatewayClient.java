package com.src.main.payment.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.src.main.payment.dto.CreateOrderRequest;
import com.src.main.payment.enums.PaymentProviderType;
import com.src.main.payment.service.AbstractHttpPaymentGatewayClient;
import com.src.main.payment.service.PaymentGatewayClient;

@Component
public class RazorpayPaymentGatewayClient extends AbstractHttpPaymentGatewayClient implements PaymentGatewayClient {
	public RazorpayPaymentGatewayClient(@Value("${app.payment.razorpay.base-url:}") String baseUrl, ObjectMapper objectMapper) {
		super(baseUrl, objectMapper);
	}

	@Override public PaymentProviderType providerType() { return PaymentProviderType.RAZORPAY; }

	@Override
	public PaymentCreateResult createPayment(String orderReference, CreateOrderRequest request) {
		return new PaymentCreateResult(orderReference + "-rzp", orderReference, null, toJson(Map.of("provider", "razorpay", "orderReference", orderReference)));
	}

	@Override
	public PaymentStatusResult fetchStatus(String paymentReference) {
		return new PaymentStatusResult("PENDING", null, toJson(Map.of("paymentReference", paymentReference, "status", "PENDING")), null);
	}
}
