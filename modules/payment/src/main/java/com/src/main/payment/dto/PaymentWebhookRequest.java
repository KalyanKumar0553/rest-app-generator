package com.src.main.payment.dto;

import jakarta.validation.constraints.Size;

public class PaymentWebhookRequest {
	@Size(max = 128) private String providerReference;
	@Size(max = 128) private String providerPaymentId;
	@Size(max = 128) private String providerOrderId;
	@Size(max = 32) private String status;
	@Size(max = 500) private String reason;
	@Size(max = 128) private String signature;
	@Size(max = 10000) private String rawPayload;

	public String getProviderReference() { return providerReference; }
	public String getProviderPaymentId() { return providerPaymentId; }
	public String getProviderOrderId() { return providerOrderId; }
	public String getStatus() { return status; }
	public String getReason() { return reason; }
	public String getSignature() { return signature; }
	public String getRawPayload() { return rawPayload; }
	public void setProviderReference(String providerReference) { this.providerReference = providerReference; }
	public void setProviderPaymentId(String providerPaymentId) { this.providerPaymentId = providerPaymentId; }
	public void setProviderOrderId(String providerOrderId) { this.providerOrderId = providerOrderId; }
	public void setStatus(String status) { this.status = status; }
	public void setReason(String reason) { this.reason = reason; }
	public void setSignature(String signature) { this.signature = signature; }
	public void setRawPayload(String rawPayload) { this.rawPayload = rawPayload; }
}
