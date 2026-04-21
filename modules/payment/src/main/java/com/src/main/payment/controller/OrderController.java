package com.src.main.payment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.src.main.payment.dto.CreateOrderRequest;
import com.src.main.payment.dto.OrderResponse;
import com.src.main.payment.service.OrderPaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/v1/orders", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrderController {
	private final OrderPaymentService orderPaymentService;
	public OrderController(OrderPaymentService orderPaymentService) { this.orderPaymentService = orderPaymentService; }
	@PostMapping
	public ResponseEntity<OrderResponse> place(@Valid @RequestBody CreateOrderRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(orderPaymentService.placeOrder(request));
	}
}
