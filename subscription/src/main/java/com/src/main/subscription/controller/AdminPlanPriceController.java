package com.src.main.subscription.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.src.main.auth.dto.common.ApiResponseDto;
import com.src.main.subscription.dto.PlanPriceRequest;
import com.src.main.subscription.dto.PlanPriceResponse;
import com.src.main.subscription.dto.ResolvedPriceResponse;
import com.src.main.subscription.enums.BillingCycle;
import com.src.main.subscription.service.PricingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.subscription", name = "admin-apis-enabled", havingValue = "true", matchIfMissing = true)
public class AdminPlanPriceController {

	private final PricingService pricingService;

	@PostMapping("/api/v1/admin/subscription/plans/{planId}/prices")
	@PreAuthorize("hasAuthority('subscription.price.manage')")
	public ResponseEntity<ApiResponseDto<PlanPriceResponse>> createPrice(@PathVariable Long planId, @Valid @RequestBody PlanPriceRequest request) {
		return ResponseEntity.ok(ApiResponseDto.ok("Price created", pricingService.createPrice(planId, request)));
	}

	@PutMapping("/api/v1/admin/subscription/prices/{priceId}")
	@PreAuthorize("hasAuthority('subscription.price.manage')")
	public ResponseEntity<ApiResponseDto<PlanPriceResponse>> updatePrice(@PathVariable Long priceId, @Valid @RequestBody PlanPriceRequest request) {
		return ResponseEntity.ok(ApiResponseDto.ok("Price updated", pricingService.updatePrice(priceId, request)));
	}

	@GetMapping("/api/v1/admin/subscription/plans/{planId}/prices")
	@PreAuthorize("hasAuthority('subscription.price.read')")
	public ResponseEntity<ApiResponseDto<List<PlanPriceResponse>>> getPlanPrices(@PathVariable Long planId) {
		return ResponseEntity.ok(ApiResponseDto.ok("OK", pricingService.getPlanPrices(planId)));
	}

	@GetMapping("/api/v1/admin/subscription/prices/resolve")
	@PreAuthorize("hasAuthority('subscription.price.read')")
	public ResponseEntity<ApiResponseDto<ResolvedPriceResponse>> resolvePrice(
			@RequestParam String planCode,
			@RequestParam BillingCycle billingCycle,
			@RequestParam String currencyCode,
			@RequestParam(name = "couponCode", required = false) String couponCode,
			@RequestParam(name = "tenantId", required = false) Long tenantId,
			@RequestParam(name = "asOf", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime asOf) {
		return ResponseEntity.ok(ApiResponseDto.ok("OK", pricingService.resolvePrice(planCode, billingCycle, currencyCode, couponCode, tenantId, asOf)));
	}
}
