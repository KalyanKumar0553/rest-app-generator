package com.src.main.payment.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.src.main.payment.enums.OrderStatus;
import com.src.main.payment.enums.PaymentStatus;

public class OrderResponse {
	private UUID orderId;
	private String orderReference;
	private OrderStatus orderStatus;
	private PaymentStatus paymentStatus;
	private String paymentProvider;
	private String paymentReference;
	private String paymentRedirectUrl;
	private BigDecimal amount;
	private String currencyCode;
	private Instant createdAt;

	public OrderResponse(UUID orderId, String orderReference, OrderStatus orderStatus, PaymentStatus paymentStatus, String paymentProvider, String paymentReference, String paymentRedirectUrl, BigDecimal amount, String currencyCode, Instant createdAt) {
		this.orderId = orderId;
		this.orderReference = orderReference;
		this.orderStatus = orderStatus;
		this.paymentStatus = paymentStatus;
		this.paymentProvider = paymentProvider;
		this.paymentReference = paymentReference;
		this.paymentRedirectUrl = paymentRedirectUrl;
		this.amount = amount;
		this.currencyCode = currencyCode;
		this.createdAt = createdAt;
	}
	public UUID getOrderId(){ return orderId; }
	public String getOrderReference(){ return orderReference; }
	public OrderStatus getOrderStatus(){ return orderStatus; }
	public PaymentStatus getPaymentStatus(){ return paymentStatus; }
	public String getPaymentProvider(){ return paymentProvider; }
	public String getPaymentReference(){ return paymentReference; }
	public String getPaymentRedirectUrl(){ return paymentRedirectUrl; }
	public BigDecimal getAmount(){ return amount; }
	public String getCurrencyCode(){ return currencyCode; }
	public Instant getCreatedAt(){ return createdAt; }
}
