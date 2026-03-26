package com.src.main.subscription.controller;

import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.src.main.auth.dto.common.ApiResponseDto;
import com.src.main.subscription.dto.CancelSubscriptionRequest;
import com.src.main.subscription.dto.EntitlementValueResponse;
import com.src.main.subscription.dto.SubscriptionContextResponse;
import com.src.main.subscription.dto.SubscriptionResponse;
import com.src.main.subscription.dto.UpgradeSubscriptionRequest;
import com.src.main.subscription.dto.UsageStatusResponse;
import com.src.main.subscription.security.SubscriptionTenantResolver;
import com.src.main.subscription.service.EntitlementService;
import com.src.main.subscription.service.SubscriptionManagementService;
import com.src.main.subscription.service.UsageTrackingService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/subscription/me")
@ConditionalOnProperty(prefix = "app.subscription", name = "self-apis-enabled", havingValue = "true", matchIfMissing = true)
public class SubscriptionSelfController {
	private final SubscriptionTenantResolver tenantResolver;
	private final SubscriptionManagementService subscriptionManagementService;
	private final EntitlementService entitlementService;
	private final UsageTrackingService usageTrackingService;

	@GetMapping
	@PreAuthorize("hasAuthority(\'subscription.self.read\')")
	public ResponseEntity<ApiResponseDto<SubscriptionContextResponse>> getMySubscription() {
		Long tenantId = tenantResolver.resolveRequiredTenantId();
		return ResponseEntity.ok(ApiResponseDto.ok("OK", entitlementService.getSubscriptionContext(tenantId)));
	}

	@GetMapping("/entitlements")
	@PreAuthorize("hasAuthority(\'subscription.self.read\')")
	public ResponseEntity<ApiResponseDto<Map<String, EntitlementValueResponse>>> getEntitlements() {
		Long tenantId = tenantResolver.resolveRequiredTenantId();
		return ResponseEntity.ok(ApiResponseDto.ok("OK", entitlementService.getAllEntitlements(tenantId)));
	}

	@GetMapping("/usage")
	@PreAuthorize("hasAuthority(\'subscription.self.read\')")
	public ResponseEntity<ApiResponseDto<UsageStatusResponse>> getUsage(@RequestParam String featureCode) {
		Long tenantId = tenantResolver.resolveRequiredTenantId();
		return ResponseEntity.ok(ApiResponseDto.ok("OK", usageTrackingService.getUsage(tenantId, featureCode)));
	}

	@PostMapping("/upgrade-request")
	@PreAuthorize("hasAuthority(\'subscription.self.upgrade\')")
	public ResponseEntity<ApiResponseDto<SubscriptionResponse>> upgrade(@Valid @RequestBody UpgradeSubscriptionRequest request) {
		request.setTenantId(tenantResolver.resolveRequiredTenantId());
		request.setUserId(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName());
		return ResponseEntity.ok(ApiResponseDto.ok("Subscription upgraded", subscriptionManagementService.upgrade(request)));
	}

	@PostMapping("/cancel-request")
	@PreAuthorize("hasAuthority(\'subscription.self.cancel\')")
	public ResponseEntity<ApiResponseDto<SubscriptionResponse>> cancel(@Valid @RequestBody CancelSubscriptionRequest request) {
		request.setTenantId(tenantResolver.resolveRequiredTenantId());
		return ResponseEntity.ok(ApiResponseDto.ok("Subscription cancellation updated", subscriptionManagementService.cancel(request)));
	}

	public SubscriptionSelfController(final SubscriptionTenantResolver tenantResolver, final SubscriptionManagementService subscriptionManagementService, final EntitlementService entitlementService, final UsageTrackingService usageTrackingService) {
		this.tenantResolver = tenantResolver;
		this.subscriptionManagementService = subscriptionManagementService;
		this.entitlementService = entitlementService;
		this.usageTrackingService = usageTrackingService;
	}
}
