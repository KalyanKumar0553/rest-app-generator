package com.src.main.payment.service;

import java.util.List;

import com.src.main.payment.dto.PaymentConfigRequest;
import com.src.main.payment.dto.PaymentConfigResponse;

public interface PaymentConfigAdminService {
	PaymentConfigResponse save(PaymentConfigRequest request);
	List<PaymentConfigResponse> findAll();
}
