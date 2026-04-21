package com.src.main.payment.dto;

import java.time.Instant;
import java.util.UUID;

import com.src.main.payment.enums.PaymentProviderType;

public class PaymentConfigResponse {
	private UUID id;
	private PaymentProviderType providerType;
	private boolean enabled;
	private boolean defaultProvider;
	private String merchantId;
	private String publicKey;
	private String endpointUrl;
	private Instant createdAt;
	private Instant updatedAt;
	public PaymentConfigResponse(UUID id, PaymentProviderType providerType, boolean enabled, boolean defaultProvider, String merchantId, String publicKey, String endpointUrl, Instant createdAt, Instant updatedAt) {
		this.id=id; this.providerType=providerType; this.enabled=enabled; this.defaultProvider=defaultProvider; this.merchantId=merchantId; this.publicKey=publicKey; this.endpointUrl=endpointUrl; this.createdAt=createdAt; this.updatedAt=updatedAt;
	}
	public UUID getId(){ return id; }
	public PaymentProviderType getProviderType(){ return providerType; }
	public boolean isEnabled(){ return enabled; }
	public boolean isDefaultProvider(){ return defaultProvider; }
	public String getMerchantId(){ return merchantId; }
	public String getPublicKey(){ return publicKey; }
	public String getEndpointUrl(){ return endpointUrl; }
	public Instant getCreatedAt(){ return createdAt; }
	public Instant getUpdatedAt(){ return updatedAt; }
}
