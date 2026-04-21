package com.src.main.payment.service;

import com.src.main.payment.dto.PaymentWebhookRequest;

public interface PaymentWebhookService {
	void processWebhook(String provider, PaymentWebhookRequest request);
}
