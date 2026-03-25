package com.src.main.subscription.controller;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.src.main.auth.dto.common.ApiResponseDto;
import com.src.main.subscription.dto.SubscriptionCouponRequest;
import com.src.main.subscription.dto.SubscriptionCouponResponse;
import com.src.main.subscription.service.SubscriptionCouponService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/subscription/coupons")
@ConditionalOnProperty(prefix = "app.subscription", name = "admin-apis-enabled", havingValue = "true", matchIfMissing = true)
public class AdminSubscriptionCouponController {

	private final SubscriptionCouponService subscriptionCouponService;

	@PostMapping
	@PreAuthorize("hasAuthority('subscription.coupon.manage')")
	public ResponseEntity<ApiResponseDto<SubscriptionCouponResponse>> createCoupon(@Valid @RequestBody SubscriptionCouponRequest request) {
		return ResponseEntity.ok(ApiResponseDto.ok("Coupon created", subscriptionCouponService.createCoupon(request)));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('subscription.coupon.manage')")
	public ResponseEntity<ApiResponseDto<SubscriptionCouponResponse>> updateCoupon(
			@PathVariable Long id,
			@Valid @RequestBody SubscriptionCouponRequest request) {
		return ResponseEntity.ok(ApiResponseDto.ok("Coupon updated", subscriptionCouponService.updateCoupon(id, request)));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('subscription.coupon.read')")
	public ResponseEntity<ApiResponseDto<SubscriptionCouponResponse>> getCoupon(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponseDto.ok("OK", subscriptionCouponService.getCoupon(id)));
	}

	@GetMapping
	@PreAuthorize("hasAuthority('subscription.coupon.read')")
	public ResponseEntity<ApiResponseDto<List<SubscriptionCouponResponse>>> getCoupons(
			@RequestParam(name = "activeOnly", required = false) Boolean activeOnly) {
		return ResponseEntity.ok(ApiResponseDto.ok("OK", subscriptionCouponService.getAllCoupons(activeOnly)));
	}

	@PatchMapping("/{id}/activate")
	@PreAuthorize("hasAuthority('subscription.coupon.manage')")
	public ResponseEntity<ApiResponseDto<Void>> activateCoupon(@PathVariable Long id) {
		subscriptionCouponService.activateCoupon(id);
		return ResponseEntity.ok(ApiResponseDto.ok("Coupon activated"));
	}

	@PatchMapping("/{id}/deactivate")
	@PreAuthorize("hasAuthority('subscription.coupon.manage')")
	public ResponseEntity<ApiResponseDto<Void>> deactivateCoupon(@PathVariable Long id) {
		subscriptionCouponService.deactivateCoupon(id);
		return ResponseEntity.ok(ApiResponseDto.ok("Coupon deactivated"));
	}
}
