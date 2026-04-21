package com.src.main.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.src.main.payment.enums.PaymentProviderType;

public class PaymentConfigRequest {
	@NotNull private PaymentProviderType providerType;
	private boolean enabled;
	private boolean defaultProvider;
	@Size(max = 200) private String merchantId;
	@Size(max = 200) private String publicKey;
	@Size(max = 4000) private String secretKey;
	@Size(max = 4000) private String webhookSecret;
	@Size(max = 500) private String endpointUrl;
	public PaymentProviderType getProviderType(){ return providerType; }
	public boolean isEnabled(){ return enabled; }
	public boolean isDefaultProvider(){ return defaultProvider; }
	public String getMerchantId(){ return merchantId; }
	public String getPublicKey(){ return publicKey; }
	public String getSecretKey(){ return secretKey; }
	public String getWebhookSecret(){ return webhookSecret; }
	public String getEndpointUrl(){ return endpointUrl; }
	public void setProviderType(PaymentProviderType providerType){ this.providerType = providerType; }
	public void setEnabled(boolean enabled){ this.enabled = enabled; }
	public void setDefaultProvider(boolean defaultProvider){ this.defaultProvider = defaultProvider; }
	public void setMerchantId(String merchantId){ this.merchantId = merchantId; }
	public void setPublicKey(String publicKey){ this.publicKey = publicKey; }
	public void setSecretKey(String secretKey){ this.secretKey = secretKey; }
	public void setWebhookSecret(String webhookSecret){ this.webhookSecret = webhookSecret; }
	public void setEndpointUrl(String endpointUrl){ this.endpointUrl = endpointUrl; }
}
