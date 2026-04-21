package com.src.main.payment.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.src.main.payment.dto.PaymentConfigRequest;
import com.src.main.payment.dto.PaymentConfigResponse;
import com.src.main.payment.service.PaymentConfigAdminService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/payment-config", produces = MediaType.APPLICATION_JSON_VALUE)
public class PaymentConfigController {
	private final PaymentConfigAdminService paymentConfigAdminService;
	public PaymentConfigController(PaymentConfigAdminService paymentConfigAdminService) { this.paymentConfigAdminService = paymentConfigAdminService; }
	@PostMapping
	public ResponseEntity<PaymentConfigResponse> save(@Valid @RequestBody PaymentConfigRequest request) { return ResponseEntity.ok(paymentConfigAdminService.save(request)); }
	@GetMapping
	public ResponseEntity<List<PaymentConfigResponse>> list() { return ResponseEntity.ok(paymentConfigAdminService.findAll()); }
}
