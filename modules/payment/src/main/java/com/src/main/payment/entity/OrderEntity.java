package com.src.main.payment.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.src.main.payment.config.PaymentDbTables;
import com.src.main.payment.enums.OrderStatus;

import jakarta.persistence.*;

@Entity
@Table(name = PaymentDbTables.ORDERS, indexes = {
		@Index(name = "idx_orders_guest_token", columnList = "guest_token"),
		@Index(name = "idx_orders_status", columnList = "status")
})
public class OrderEntity {
	@Id @Column(name = "id", nullable = false, updatable = false) private UUID id;
	@Column(name = "guest_token", nullable = false, length = 128) private String guestToken;
	@Column(name = "order_reference", nullable = false, length = 64, unique = true) private String orderReference;
	@Column(name = "currency_code", nullable = false, length = 10) private String currencyCode;
	@Column(name = "amount", nullable = false, precision = 19, scale = 2) private BigDecimal amount;
	@Enumerated(EnumType.STRING) @Column(name = "status", nullable = false, length = 32) private OrderStatus status;
	@Column(name = "payment_provider", length = 32) private String paymentProvider;
	@Column(name = "payment_reference", length = 128) private String paymentReference;
	@Column(name = "payment_status", length = 32) private String paymentStatus;
	@Column(name = "payment_attempt_count", nullable = false) private int paymentAttemptCount;
	@Column(name = "payment_last_checked_at") private Instant paymentLastCheckedAt;
	@Column(name = "payment_completed_at") private Instant paymentCompletedAt;
	@Column(name = "payment_failure_reason", length = 500) private String paymentFailureReason;
	@Column(name = "payload_json", columnDefinition = "text") private String payloadJson;
	@Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
	@Column(name = "updated_at", nullable = false) private Instant updatedAt;
	@PrePersist void prePersist(){ if (id == null) id = UUID.randomUUID(); Instant now = Instant.now(); createdAt = now; updatedAt = now; if (status == null) status = OrderStatus.PENDING_PAYMENT; }
	@PreUpdate void preUpdate(){ updatedAt = Instant.now(); }
	public UUID getId(){ return id; }
	public String getGuestToken(){ return guestToken; }
	public String getOrderReference(){ return orderReference; }
	public String getCurrencyCode(){ return currencyCode; }
	public BigDecimal getAmount(){ return amount; }
	public OrderStatus getStatus(){ return status; }
	public String getPaymentProvider(){ return paymentProvider; }
	public String getPaymentReference(){ return paymentReference; }
	public String getPaymentStatus(){ return paymentStatus; }
	public int getPaymentAttemptCount(){ return paymentAttemptCount; }
	public Instant getPaymentLastCheckedAt(){ return paymentLastCheckedAt; }
	public Instant getPaymentCompletedAt(){ return paymentCompletedAt; }
	public String getPaymentFailureReason(){ return paymentFailureReason; }
	public String getPayloadJson(){ return payloadJson; }
	public Instant getCreatedAt(){ return createdAt; }
	public Instant getUpdatedAt(){ return updatedAt; }
	public void setGuestToken(String guestToken){ this.guestToken = guestToken; }
	public void setOrderReference(String orderReference){ this.orderReference = orderReference; }
	public void setCurrencyCode(String currencyCode){ this.currencyCode = currencyCode; }
	public void setAmount(BigDecimal amount){ this.amount = amount; }
	public void setStatus(OrderStatus status){ this.status = status; }
	public void setPaymentProvider(String paymentProvider){ this.paymentProvider = paymentProvider; }
	public void setPaymentReference(String paymentReference){ this.paymentReference = paymentReference; }
	public void setPaymentStatus(String paymentStatus){ this.paymentStatus = paymentStatus; }
	public void setPaymentAttemptCount(int paymentAttemptCount){ this.paymentAttemptCount = paymentAttemptCount; }
	public void setPaymentLastCheckedAt(Instant paymentLastCheckedAt){ this.paymentLastCheckedAt = paymentLastCheckedAt; }
	public void setPaymentCompletedAt(Instant paymentCompletedAt){ this.paymentCompletedAt = paymentCompletedAt; }
	public void setPaymentFailureReason(String paymentFailureReason){ this.paymentFailureReason = paymentFailureReason; }
	public void setPayloadJson(String payloadJson){ this.payloadJson = payloadJson; }
}
