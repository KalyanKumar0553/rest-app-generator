package com.src.main.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.src.main.payment.dto.PaymentWebhookRequest;
import com.src.main.payment.service.PaymentWebhookService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/v1/payments/webhook", produces = MediaType.APPLICATION_JSON_VALUE)
public class PaymentWebhookController {
	private final PaymentWebhookService paymentWebhookService;

	public PaymentWebhookController(PaymentWebhookService paymentWebhookService) {
		this.paymentWebhookService = paymentWebhookService;
	}

	@PostMapping("/{provider}")
	public ResponseEntity<Void> receive(@PathVariable("provider") String provider, @Valid @RequestBody PaymentWebhookRequest request) {
		paymentWebhookService.processWebhook(provider, request);
		return ResponseEntity.ok().build();
	}
}
