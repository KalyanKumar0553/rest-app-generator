package com.src.main.payment.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateOrderRequest {
	@NotBlank @Size(max = 128) private String guestToken;
	@NotBlank @Size(max = 10) private String currencyCode;
	@NotNull @DecimalMin("0.01") private BigDecimal amount;
	@Size(max = 5000) private String payloadJson;

	public String getGuestToken(){ return guestToken; }
	public String getCurrencyCode(){ return currencyCode; }
	public BigDecimal getAmount(){ return amount; }
	public String getPayloadJson(){ return payloadJson; }
	public void setGuestToken(String guestToken){ this.guestToken = guestToken; }
	public void setCurrencyCode(String currencyCode){ this.currencyCode = currencyCode; }
	public void setAmount(BigDecimal amount){ this.amount = amount; }
	public void setPayloadJson(String payloadJson){ this.payloadJson = payloadJson; }
}
