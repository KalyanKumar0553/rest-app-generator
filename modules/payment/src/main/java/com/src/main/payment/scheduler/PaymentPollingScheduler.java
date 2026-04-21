package com.src.main.payment.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.src.main.payment.service.OrderPaymentService;

@Component
public class PaymentPollingScheduler {
	private final OrderPaymentService orderPaymentService;
	public PaymentPollingScheduler(OrderPaymentService orderPaymentService) { this.orderPaymentService = orderPaymentService; }
	@Scheduled(fixedDelayString = "${app.payment.polling.fixed-delay-ms:30000}")
	public void pollPayments() { orderPaymentService.pollAndReconcile(); }
}
