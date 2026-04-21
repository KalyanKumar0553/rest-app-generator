package com.src.main.payment.service;

import com.src.main.payment.dto.CreateOrderRequest;
import com.src.main.payment.dto.OrderResponse;

public interface OrderPaymentService {
	OrderResponse placeOrder(CreateOrderRequest request);
	void pollAndReconcile();
}
