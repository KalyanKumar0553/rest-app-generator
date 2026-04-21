package com.src.main.payment.entity;

import java.time.Instant;
import java.util.UUID;

import com.src.main.payment.config.PaymentDbTables;
import com.src.main.payment.enums.PaymentProviderType;

import jakarta.persistence.*;

@Entity
@Table(name = PaymentDbTables.PAYMENT_CONFIGS, uniqueConstraints = @UniqueConstraint(name = "uq_payment_config_provider", columnNames = "provider_type"))
public class PaymentConfigEntity {
	@Id @Column(name = "id", nullable = false, updatable = false)
	private UUID id;
	@Enumerated(EnumType.STRING)
	@Column(name = "provider_type", nullable = false, length = 32)
	private PaymentProviderType providerType;
	@Column(name = "enabled", nullable = false)
	private boolean enabled;
	@Column(name = "is_default", nullable = false)
	private boolean defaultProvider;
	@Column(name = "merchant_id", length = 200)
	private String merchantId;
	@Column(name = "public_key", length = 200)
	private String publicKey;
	@Column(name = "secret_key_encrypted", length = 4000)
	private String secretKeyEncrypted;
	@Column(name = "secret_key_hash", length = 128)
	private String secretKeyHash;
	@Column(name = "secret_key_salt", length = 128)
	private String secretKeySalt;
	@Column(name = "webhook_secret_encrypted", length = 4000)
	private String webhookSecretEncrypted;
	@Column(name = "webhook_secret_hash", length = 128)
	private String webhookSecretHash;
	@Column(name = "webhook_secret_salt", length = 128)
	private String webhookSecretSalt;
	@Column(name = "endpoint_url", length = 500)
	private String endpointUrl;
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
	@PrePersist void prePersist(){ if (id == null) id = UUID.randomUUID(); Instant now = Instant.now(); createdAt = now; updatedAt = now; }
	@PreUpdate void preUpdate(){ updatedAt = Instant.now(); }
	public UUID getId(){ return id; }
	public PaymentProviderType getProviderType(){ return providerType; }
	public boolean isEnabled(){ return enabled; }
	public boolean isDefaultProvider(){ return defaultProvider; }
	public String getMerchantId(){ return merchantId; }
	public String getPublicKey(){ return publicKey; }
	public String getSecretKeyEncrypted(){ return secretKeyEncrypted; }
	public String getSecretKeyHash(){ return secretKeyHash; }
	public String getSecretKeySalt(){ return secretKeySalt; }
	public String getWebhookSecretEncrypted(){ return webhookSecretEncrypted; }
	public String getWebhookSecretHash(){ return webhookSecretHash; }
	public String getWebhookSecretSalt(){ return webhookSecretSalt; }
	public String getEndpointUrl(){ return endpointUrl; }
	public Instant getCreatedAt(){ return createdAt; }
	public Instant getUpdatedAt(){ return updatedAt; }
	public void setProviderType(PaymentProviderType providerType){ this.providerType = providerType; }
	public void setEnabled(boolean enabled){ this.enabled = enabled; }
	public void setDefaultProvider(boolean defaultProvider){ this.defaultProvider = defaultProvider; }
	public void setMerchantId(String merchantId){ this.merchantId = merchantId; }
	public void setPublicKey(String publicKey){ this.publicKey = publicKey; }
	public void setSecretKeyEncrypted(String secretKeyEncrypted){ this.secretKeyEncrypted = secretKeyEncrypted; }
	public void setSecretKeyHash(String secretKeyHash){ this.secretKeyHash = secretKeyHash; }
	public void setSecretKeySalt(String secretKeySalt){ this.secretKeySalt = secretKeySalt; }
	public void setWebhookSecretEncrypted(String webhookSecretEncrypted){ this.webhookSecretEncrypted = webhookSecretEncrypted; }
	public void setWebhookSecretHash(String webhookSecretHash){ this.webhookSecretHash = webhookSecretHash; }
	public void setWebhookSecretSalt(String webhookSecretSalt){ this.webhookSecretSalt = webhookSecretSalt; }
	public void setEndpointUrl(String endpointUrl){ this.endpointUrl = endpointUrl; }
}
