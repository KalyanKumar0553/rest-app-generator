package com.src.main.payment.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.src.main.payment.dto.CreateOrderRequest;
import com.src.main.payment.dto.OrderResponse;
import com.src.main.payment.entity.OrderEntity;
import com.src.main.payment.entity.PaymentEntity;
import com.src.main.payment.enums.OrderStatus;
import com.src.main.payment.enums.PaymentProviderType;
import com.src.main.payment.enums.PaymentStatus;
import com.src.main.payment.exception.PaymentConfigurationException;
import com.src.main.payment.exception.PaymentErrorCode;
import com.src.main.payment.exception.PaymentProviderException;
import com.src.main.payment.exception.PaymentStateException;
import com.src.main.payment.exception.PaymentValidationException;
import com.src.main.payment.repository.OrderRepository;
import com.src.main.payment.repository.PaymentRepository;
import com.src.main.payment.service.OrderPaymentService;
import com.src.main.payment.service.PaymentConfigService;
import com.src.main.payment.service.PaymentStateGuard;
import com.src.main.payment.service.PaymentGatewayClient;
import com.src.main.payment.service.PaymentGatewayRegistry;

@Service
public class OrderPaymentServiceImpl implements OrderPaymentService {
	private final OrderRepository orderRepository;
	private final PaymentRepository paymentRepository;
	private final PaymentConfigService paymentConfigService;
	private final PaymentGatewayRegistry gatewayRegistry;
	private final ObjectMapper objectMapper;

	public OrderPaymentServiceImpl(OrderRepository orderRepository, PaymentRepository paymentRepository, PaymentConfigService paymentConfigService, PaymentGatewayRegistry gatewayRegistry, ObjectMapper objectMapper) {
		this.orderRepository = orderRepository;
		this.paymentRepository = paymentRepository;
		this.paymentConfigService = paymentConfigService;
		this.gatewayRegistry = gatewayRegistry;
		this.objectMapper = objectMapper;
	}

	@Override
	@Transactional
	public OrderResponse placeOrder(CreateOrderRequest request) {
		var config = paymentConfigService.getDefaultEnabledConfig();
		PaymentProviderType providerType = config.getProviderType();
		PaymentGatewayClient gatewayClient = gatewayRegistry.resolve(providerType);
		String orderReference = "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 18).toUpperCase();

		OrderEntity order = new OrderEntity();
		order.setGuestToken(hashGuestToken(request.getGuestToken()));
		order.setOrderReference(orderReference);
		order.setCurrencyCode(request.getCurrencyCode().trim().toUpperCase());
		order.setAmount(request.getAmount());
		order.setStatus(OrderStatus.PENDING_PAYMENT);
		order.setPaymentProvider(providerType.name());
		order.setPayloadJson(normalizePayload(request.getPayloadJson()));
		order = orderRepository.save(order);

		PaymentGatewayClient.PaymentCreateResult paymentResult = gatewayClient.createPayment(orderReference, request);

		PaymentEntity payment = new PaymentEntity();
		payment.setOrderId(order.getId());
		payment.setProviderType(providerType.name());
		payment.setStatus(PaymentStatus.INITIATED);
		payment.setProviderReference(paymentResult.paymentReference());
		payment.setProviderOrderId(paymentResult.providerOrderId());
		payment.setRawResponseJson(paymentResult.rawResponseJson());
		payment.setNextPollAt(Instant.now().plusSeconds(30));
		paymentRepository.save(payment);

		order.setPaymentReference(paymentResult.paymentReference());
		order.setPaymentStatus(PaymentStatus.INITIATED.name());
		orderRepository.save(order);

		return new OrderResponse(order.getId(), order.getOrderReference(), order.getStatus(), PaymentStatus.INITIATED, providerType.name(), paymentResult.paymentReference(), paymentResult.paymentRedirectUrl(), order.getAmount(), order.getCurrencyCode(), order.getCreatedAt());
	}

	@Override
	@Transactional
	public void pollAndReconcile() {
		List<PaymentEntity> duePayments = paymentRepository.findTop100ByStatusInAndNextPollAtBeforeOrderByNextPollAtAsc(
				List.of(PaymentStatus.INITIATED, PaymentStatus.PENDING),
				Instant.now());
		for (PaymentEntity payment : duePayments) {
			try {
				PaymentStateGuard.requirePaymentMutable(payment);
				PaymentGatewayClient gateway = gatewayRegistry.resolve(PaymentProviderType.valueOf(payment.getProviderType()));
				var status = gateway.fetchStatus(payment.getProviderReference());
				payment.setLastPolledAt(Instant.now());
				payment.setRawResponseJson(status.rawResponseJson());
				payment.setStatusReason(status.reason());
				switch (normalizeStatus(status.status())) {
					case "SUCCEEDED" -> {
						payment.setStatus(PaymentStatus.SUCCEEDED);
						payment.setNextPollAt(null);
						OrderEntity order = orderRepository.findById(payment.getOrderId()).orElseThrow(() -> new PaymentStateException(PaymentErrorCode.ORDER_NOT_FOUND, "Order not found for payment " + payment.getId()));
						order.setStatus(OrderStatus.PAID);
						order.setPaymentStatus(PaymentStatus.SUCCEEDED.name());
						order.setPaymentCompletedAt(Instant.now());
						orderRepository.save(order);
					}
					case "FAILED", "EXPIRED" -> {
						payment.setStatus(PaymentStatus.valueOf(normalizeStatus(status.status())));
						payment.setNextPollAt(null);
						OrderEntity order = orderRepository.findById(payment.getOrderId()).orElseThrow(() -> new PaymentStateException(PaymentErrorCode.ORDER_NOT_FOUND, "Order not found for payment " + payment.getId()));
						order.setStatus(OrderStatus.FAILED);
						order.setPaymentStatus(payment.getStatus().name());
						order.setPaymentFailureReason(status.reason());
						orderRepository.save(order);
					}
					default -> payment.setNextPollAt(Instant.now().plusSeconds(30));
				}
				payment.setRetryCount(payment.getRetryCount() + 1);
				paymentRepository.save(payment);
			} catch (Exception ex) {
				payment.setRetryCount(payment.getRetryCount() + 1);
				payment.setNextPollAt(Instant.now().plusSeconds(60));
				payment.setStatusReason(ex.getMessage());
				paymentRepository.save(payment);
			}
		}
	}

	private String hashGuestToken(String guestToken) {
		try {
			byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
					.digest(guestToken.trim().getBytes(java.nio.charset.StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder(digest.length * 2);
			for (byte b : digest) {
				hex.append(String.format("%02x", b));
			}
			return hex.toString();
		} catch (Exception ex) {
			throw new PaymentProviderException(PaymentErrorCode.GATEWAY_FAILURE, "Failed to secure guest token", false, ex);
		}
	}

	private String normalizePayload(String payloadJson) {
		if (payloadJson == null || payloadJson.isBlank()) return null;
		try {
			return objectMapper.writeValueAsString(objectMapper.readTree(payloadJson));
		} catch (Exception ex) {
			throw new com.src.main.payment.exception.PaymentValidationException(PaymentErrorCode.WEBHOOK_INVALID_PAYLOAD, "Invalid order payload JSON");
		}
	}

	private String normalizeStatus(String status) {
		return status == null ? "PENDING" : status.trim().toUpperCase();
	}
}
