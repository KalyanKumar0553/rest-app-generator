package com.src.main.payment.entity;

import java.time.Instant;
import java.util.UUID;

import com.src.main.payment.config.PaymentDbTables;
import com.src.main.payment.enums.PaymentStatus;

import jakarta.persistence.*;

@Entity
@Table(name = PaymentDbTables.PAYMENTS, indexes = {
		@Index(name = "idx_payments_order_id", columnList = "order_id"),
		@Index(name = "idx_payments_provider_ref", columnList = "provider_reference"),
		@Index(name = "idx_payments_status", columnList = "status")
})
public class PaymentEntity {
	@Id @Column(name = "id", nullable = false, updatable = false) private UUID id;
	@Column(name = "order_id", nullable = false) private UUID orderId;
	@Column(name = "provider_type", nullable = false, length = 32) private String providerType;
	@Enumerated(EnumType.STRING) @Column(name = "status", nullable = false, length = 32) private PaymentStatus status;
	@Column(name = "provider_reference", length = 128) private String providerReference;
	@Column(name = "provider_payment_id", length = 128) private String providerPaymentId;
	@Column(name = "provider_order_id", length = 128) private String providerOrderId;
	@Column(name = "provider_signature_hash", length = 128) private String providerSignatureHash;
	@Column(name = "status_reason", length = 500) private String statusReason;
	@Column(name = "retry_count", nullable = false) private int retryCount;
	@Column(name = "next_poll_at") private Instant nextPollAt;
	@Column(name = "last_polled_at") private Instant lastPolledAt;
	@Column(name = "raw_response_json", columnDefinition = "text") private String rawResponseJson;
	@Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
	@Column(name = "updated_at", nullable = false) private Instant updatedAt;
	@PrePersist void prePersist(){ if (id == null) id = UUID.randomUUID(); Instant now = Instant.now(); createdAt = now; updatedAt = now; if (status == null) status = PaymentStatus.INITIATED; }
	@PreUpdate void preUpdate(){ updatedAt = Instant.now(); }
	public UUID getId(){ return id; }
	public UUID getOrderId(){ return orderId; }
	public String getProviderType(){ return providerType; }
	public PaymentStatus getStatus(){ return status; }
	public String getProviderReference(){ return providerReference; }
	public String getProviderPaymentId(){ return providerPaymentId; }
	public String getProviderOrderId(){ return providerOrderId; }
	public String getProviderSignatureHash(){ return providerSignatureHash; }
	public String getStatusReason(){ return statusReason; }
	public int getRetryCount(){ return retryCount; }
	public Instant getNextPollAt(){ return nextPollAt; }
	public Instant getLastPolledAt(){ return lastPolledAt; }
	public String getRawResponseJson(){ return rawResponseJson; }
	public Instant getCreatedAt(){ return createdAt; }
	public Instant getUpdatedAt(){ return updatedAt; }
	public void setOrderId(UUID orderId){ this.orderId = orderId; }
	public void setProviderType(String providerType){ this.providerType = providerType; }
	public void setStatus(PaymentStatus status){ this.status = status; }
	public void setProviderReference(String providerReference){ this.providerReference = providerReference; }
	public void setProviderPaymentId(String providerPaymentId){ this.providerPaymentId = providerPaymentId; }
	public void setProviderOrderId(String providerOrderId){ this.providerOrderId = providerOrderId; }
	public void setProviderSignatureHash(String providerSignatureHash){ this.providerSignatureHash = providerSignatureHash; }
	public void setStatusReason(String statusReason){ this.statusReason = statusReason; }
	public void setRetryCount(int retryCount){ this.retryCount = retryCount; }
	public void setNextPollAt(Instant nextPollAt){ this.nextPollAt = nextPollAt; }
	public void setLastPolledAt(Instant lastPolledAt){ this.lastPolledAt = lastPolledAt; }
	public void setRawResponseJson(String rawResponseJson){ this.rawResponseJson = rawResponseJson; }
}
