package com.src.main.subscription.controller;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.src.main.auth.dto.common.ApiResponseDto;
import com.src.main.subscription.dto.SubscriptionOverrideRequest;
import com.src.main.subscription.dto.SubscriptionOverrideResponse;
import com.src.main.subscription.service.SubscriptionOverrideService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/subscription/tenants/{tenantId}/overrides")
@ConditionalOnProperty(prefix = "app.subscription", name = "admin-apis-enabled", havingValue = "true", matchIfMissing = true)
public class AdminSubscriptionOverrideController {

	private final SubscriptionOverrideService subscriptionOverrideService;

	@PostMapping
	@PreAuthorize("hasAuthority('subscription.override.manage')")
	public ResponseEntity<ApiResponseDto<SubscriptionOverrideResponse>> create(
			@PathVariable Long tenantId,
			@Valid @RequestBody SubscriptionOverrideRequest request) {
		return ResponseEntity.ok(ApiResponseDto.ok("Override created", subscriptionOverrideService.createOverride(tenantId, request)));
	}

	@PutMapping("/{overrideId}")
	@PreAuthorize("hasAuthority('subscription.override.manage')")
	public ResponseEntity<ApiResponseDto<SubscriptionOverrideResponse>> update(
			@PathVariable Long tenantId,
			@PathVariable Long overrideId,
			@Valid @RequestBody SubscriptionOverrideRequest request) {
		return ResponseEntity.ok(ApiResponseDto.ok("Override updated", subscriptionOverrideService.updateOverride(tenantId, overrideId, request)));
	}

	@GetMapping
	@PreAuthorize("hasAuthority('subscription.override.manage')")
	public ResponseEntity<ApiResponseDto<List<SubscriptionOverrideResponse>>> list(@PathVariable Long tenantId) {
		return ResponseEntity.ok(ApiResponseDto.ok("OK", subscriptionOverrideService.getOverrides(tenantId)));
	}

	@DeleteMapping("/{overrideId}")
	@PreAuthorize("hasAuthority('subscription.override.manage')")
	public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable Long tenantId, @PathVariable Long overrideId) {
		subscriptionOverrideService.deleteOverride(tenantId, overrideId);
		return ResponseEntity.ok(ApiResponseDto.ok("Override deleted"));
	}
}
