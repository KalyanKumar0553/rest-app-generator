package com.src.main.subscription.controller;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.src.main.auth.dto.common.ApiResponseDto;
import com.src.main.subscription.dto.CancelSubscriptionRequest;
import com.src.main.subscription.dto.DowngradeSubscriptionRequest;
import com.src.main.subscription.dto.RenewSubscriptionRequest;
import com.src.main.subscription.dto.StartTrialRequest;
import com.src.main.subscription.dto.SubscriptionAuditResponse;
import com.src.main.subscription.dto.SubscriptionRequest;
import com.src.main.subscription.dto.SubscriptionResponse;
import com.src.main.subscription.dto.UpgradeSubscriptionRequest;
import com.src.main.subscription.service.SubscriptionAuditService;
import com.src.main.subscription.service.SubscriptionManagementService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/subscription")
@ConditionalOnProperty(prefix = "app.subscription", name = "admin-apis-enabled", havingValue = "true", matchIfMissing = true)
public class AdminSubscriptionManagementController {

	private final SubscriptionManagementService subscriptionManagementService;
	private final SubscriptionAuditService subscriptionAuditService;

	@PostMapping("/assign-default")
	@PreAuthorize("hasAuthority('subscription.manage')")
	public ResponseEntity<ApiResponseDto<SubscriptionResponse>> assignDefault(@RequestBody CancelSubscriptionRequest request) {
		return ResponseEntity.ok(ApiResponseDto.ok("Default plan assigned", subscriptionManagementService.assignDefaultPlan(request.getTenantId())));
	}

	@PostMapping("/subscribe")
	@PreAuthorize("hasAuthority('subscription.manage')")
	public ResponseEntity<ApiResponseDto<SubscriptionResponse>> subscribe(@Valid @RequestBody SubscriptionRequest request) {
		return ResponseEntity.ok(ApiResponseDto.ok("Subscription created", subscriptionManagementService.subscribe(request)));
	}

	@PostMapping("/start-trial")
	@PreAuthorize("hasAuthority('subscription.manage')")
	public ResponseEntity<ApiResponseDto<SubscriptionResponse>> startTrial(@Valid @RequestBody StartTrialRequest request) {
		return ResponseEntity.ok(ApiResponseDto.ok("Trial started", subscriptionManagementService.startTrial(request)));
	}

	@PostMapping("/upgrade")
	@PreAuthorize("hasAuthority('subscription.manage')")
	public ResponseEntity<ApiResponseDto<SubscriptionResponse>> upgrade(@Valid @RequestBody UpgradeSubscriptionRequest request) {
		return ResponseEntity.ok(ApiResponseDto.ok("Subscription upgraded", subscriptionManagementService.upgrade(request)));
	}

	@PostMapping("/downgrade")
	@PreAuthorize("hasAuthority('subscription.manage')")
	public ResponseEntity<ApiResponseDto<SubscriptionResponse>> downgrade(@Valid @RequestBody DowngradeSubscriptionRequest request) {
		return ResponseEntity.ok(ApiResponseDto.ok("Downgrade scheduled", subscriptionManagementService.scheduleDowngrade(request)));
	}

	@PostMapping("/cancel")
	@PreAuthorize("hasAuthority('subscription.manage')")
	public ResponseEntity<ApiResponseDto<SubscriptionResponse>> cancel(@Valid @RequestBody CancelSubscriptionRequest request) {
		return ResponseEntity.ok(ApiResponseDto.ok("Subscription cancelled", subscriptionManagementService.cancel(request)));
	}

	@PostMapping("/renew")
	@PreAuthorize("hasAuthority('subscription.manage')")
	public ResponseEntity<ApiResponseDto<SubscriptionResponse>> renew(@Valid @RequestBody RenewSubscriptionRequest request) {
		return ResponseEntity.ok(ApiResponseDto.ok("Subscription renewed", subscriptionManagementService.renew(request)));
	}

	@GetMapping("/tenants/{tenantId}/current")
	@PreAuthorize("hasAuthority('subscription.manage')")
	public ResponseEntity<ApiResponseDto<SubscriptionResponse>> getCurrent(@PathVariable Long tenantId) {
		return ResponseEntity.ok(ApiResponseDto.ok("OK", subscriptionManagementService.getCurrentSubscription(tenantId)));
	}

	@GetMapping("/tenants/{tenantId}/history")
	@PreAuthorize("hasAuthority('subscription.manage')")
	public ResponseEntity<ApiResponseDto<List<SubscriptionResponse>>> history(@PathVariable Long tenantId) {
		return ResponseEntity.ok(ApiResponseDto.ok("OK", subscriptionManagementService.getSubscriptionHistory(tenantId)));
	}

	@GetMapping("/tenants/{tenantId}/audit")
	@PreAuthorize("hasAuthority('subscription.audit.read')")
	public ResponseEntity<ApiResponseDto<List<SubscriptionAuditResponse>>> audit(@PathVariable Long tenantId) {
		return ResponseEntity.ok(ApiResponseDto.ok("OK", subscriptionAuditService.getAuditHistory(tenantId)));
	}
}
