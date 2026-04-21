package com.src.main.payment.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.payment.dto.PaymentWebhookRequest;
import com.src.main.payment.entity.OrderEntity;
import com.src.main.payment.entity.PaymentConfigEntity;
import com.src.main.payment.entity.PaymentEntity;
import com.src.main.payment.enums.OrderStatus;
import com.src.main.payment.enums.PaymentProviderType;
import com.src.main.payment.enums.PaymentStatus;
import com.src.main.payment.exception.PaymentConfigurationException;
import com.src.main.payment.exception.PaymentErrorCode;
import com.src.main.payment.exception.PaymentStateException;
import com.src.main.payment.exception.PaymentValidationException;
import com.src.main.payment.repository.OrderRepository;
import com.src.main.payment.repository.PaymentRepository;
import com.src.main.payment.service.PaymentWebhookService;
import com.src.main.payment.service.PaymentConfigService;

@Service
public class PaymentWebhookServiceImpl implements PaymentWebhookService {
	private final PaymentRepository paymentRepository;
	private final OrderRepository orderRepository;
	private final PaymentConfigService paymentConfigService;

	public PaymentWebhookServiceImpl(PaymentRepository paymentRepository, OrderRepository orderRepository, PaymentConfigService paymentConfigService) {
		this.paymentRepository = paymentRepository;
		this.orderRepository = orderRepository;
		this.paymentConfigService = paymentConfigService;
	}

	@Override
	@Transactional
	public void processWebhook(String provider, PaymentWebhookRequest request) {
		PaymentProviderType providerType = parseProvider(provider);
		PaymentConfigEntity config = paymentConfigService.getEnabledConfig(providerType)
				.orElseThrow(() -> new PaymentConfigurationException(PaymentErrorCode.CONFIG_NOT_FOUND, "Payment provider is not configured: " + providerType));
		verifySignature(config, request);
		if (request.getProviderReference() == null || request.getProviderReference().isBlank()) {
			throw new PaymentValidationException(PaymentErrorCode.WEBHOOK_INVALID_PAYLOAD, "providerReference is required.");
		}
		PaymentEntity payment = paymentRepository.findFirstByProviderReference(request.getProviderReference())
				.orElseThrow(() -> new PaymentStateException(PaymentErrorCode.PAYMENT_NOT_FOUND, "Payment record not found for webhook reference."));
		if (payment.getStatus() == PaymentStatus.SUCCEEDED || payment.getStatus() == PaymentStatus.FAILED || payment.getStatus() == PaymentStatus.EXPIRED) {
			throw new PaymentStateException(PaymentErrorCode.PAYMENT_INVALID_STATE, "Payment is already in a terminal state.");
		}
		payment.setProviderPaymentId(request.getProviderPaymentId());
		payment.setProviderOrderId(request.getProviderOrderId());
		payment.setRawResponseJson(request.getRawPayload());
		payment.setStatusReason(request.getReason());
		PaymentStatus status = parsePaymentStatus(request.getStatus());
		if (status == PaymentStatus.INITIATED || status == PaymentStatus.PENDING) {
			throw new PaymentValidationException(PaymentErrorCode.WEBHOOK_INVALID_PAYLOAD, "Webhook must provide a terminal payment status.");
		}
		payment.setStatus(status);
		paymentRepository.save(payment);

		OrderEntity order = orderRepository.findById(payment.getOrderId())
				.orElseThrow(() -> new PaymentStateException(PaymentErrorCode.ORDER_NOT_FOUND, "Order not found for payment webhook."));
		if (status == PaymentStatus.SUCCEEDED) {
			if (order.getStatus() == OrderStatus.PAID) {
				return;
			}
			order.setStatus(OrderStatus.PAID);
			order.setPaymentStatus(PaymentStatus.SUCCEEDED.name());
			order.setPaymentCompletedAt(java.time.Instant.now());
		} else if (status == PaymentStatus.FAILED || status == PaymentStatus.EXPIRED) {
			if (order.getStatus() == OrderStatus.PAID) {
				throw new PaymentStateException(PaymentErrorCode.ORDER_INVALID_STATE, "Paid order cannot be moved to failed.");
			}
			order.setStatus(OrderStatus.FAILED);
			order.setPaymentStatus(status.name());
			order.setPaymentFailureReason(request.getReason());
		}
		orderRepository.save(order);
	}

	private void verifySignature(PaymentConfigEntity config, PaymentWebhookRequest request) {
		String incoming = request.getSignature();
		String secretHash = config.getWebhookSecretHash();
		if (secretHash == null || secretHash.isBlank()) {
			throw new PaymentConfigurationException(PaymentErrorCode.CONFIG_INVALID, "Webhook secret is not configured.");
		}
		if (incoming == null || incoming.isBlank() || !incoming.equals(secretHash)) {
			throw new PaymentStateException(PaymentErrorCode.WEBHOOK_INVALID_SIGNATURE, "Invalid payment webhook signature.");
		}
	}

	private PaymentProviderType parseProvider(String provider) {
		try {
			return PaymentProviderType.valueOf(provider.trim().toUpperCase());
		} catch (Exception ex) {
			throw new PaymentValidationException(PaymentErrorCode.CONFIG_INVALID, "Unsupported payment provider: " + provider);
		}
	}

	private PaymentStatus parsePaymentStatus(String status) {
		if (status == null) {
			return PaymentStatus.PENDING;
		}
		try {
			return PaymentStatus.valueOf(status.trim().toUpperCase());
		} catch (Exception ex) {
			throw new PaymentValidationException(PaymentErrorCode.WEBHOOK_INVALID_PAYLOAD, "Unsupported payment status: " + status);
		}
	}
}
