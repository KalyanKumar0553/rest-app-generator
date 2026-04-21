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
public class StripePaymentGatewayClient extends AbstractHttpPaymentGatewayClient implements PaymentGatewayClient {
	public StripePaymentGatewayClient(@Value("${app.payment.stripe.base-url:}") String baseUrl, ObjectMapper objectMapper) { super(baseUrl, objectMapper); }
	@Override public PaymentProviderType providerType() { return PaymentProviderType.STRIPE; }
	@Override public PaymentCreateResult createPayment(String orderReference, CreateOrderRequest request) { return new PaymentCreateResult(orderReference + "-str", orderReference, null, toJson(Map.of("provider", "stripe", "orderReference", orderReference))); }
	@Override public PaymentStatusResult fetchStatus(String paymentReference) { return new PaymentStatusResult("PENDING", null, toJson(Map.of("paymentReference", paymentReference, "status", "PENDING")), null); }
}
