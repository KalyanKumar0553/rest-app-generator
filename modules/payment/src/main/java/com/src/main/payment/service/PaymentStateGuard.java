package com.src.main.payment.service;

import com.src.main.payment.entity.OrderEntity;
import com.src.main.payment.entity.PaymentEntity;
import com.src.main.payment.enums.OrderStatus;
import com.src.main.payment.enums.PaymentStatus;
import com.src.main.payment.exception.PaymentErrorCode;
import com.src.main.payment.exception.PaymentStateException;

public final class PaymentStateGuard {
	private PaymentStateGuard() {}

	public static void requireOrderOpenForPayment(OrderEntity order) {
		if (order.getStatus() != null && order.getStatus() != OrderStatus.PENDING_PAYMENT) {
			throw new PaymentStateException(PaymentErrorCode.ORDER_INVALID_STATE, "Order is not open for payment.");
		}
	}

	public static void requirePaymentMutable(PaymentEntity payment) {
		if (payment.getStatus() == PaymentStatus.SUCCEEDED || payment.getStatus() == PaymentStatus.FAILED || payment.getStatus() == PaymentStatus.EXPIRED) {
			throw new PaymentStateException(PaymentErrorCode.PAYMENT_INVALID_STATE, "Payment is already in a terminal state.");
		}
	}

	public static void requireWebhookTerminalStatus(PaymentStatus status) {
		if (status == PaymentStatus.INITIATED || status == PaymentStatus.PENDING) {
			throw new PaymentStateException(PaymentErrorCode.WEBHOOK_INVALID_PAYLOAD, "Webhook must carry a terminal payment status.");
		}
	}
}
