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
import com.src.main.subscription.dto.PlanRequest;
import com.src.main.subscription.dto.PlanResponse;
import com.src.main.subscription.service.SubscriptionPlanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/subscription/plans")
@ConditionalOnProperty(prefix = "app.subscription", name = "admin-apis-enabled", havingValue = "true", matchIfMissing = true)
public class AdminSubscriptionPlanController {

	private final SubscriptionPlanService subscriptionPlanService;

	@PostMapping
	@PreAuthorize("hasAuthority('subscription.plan.manage')")
	public ResponseEntity<ApiResponseDto<PlanResponse>> createPlan(@Valid @RequestBody PlanRequest request) {
		return ResponseEntity.ok(ApiResponseDto.ok("Plan created", subscriptionPlanService.createPlan(request)));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('subscription.plan.manage')")
	public ResponseEntity<ApiResponseDto<PlanResponse>> updatePlan(@PathVariable Long id, @Valid @RequestBody PlanRequest request) {
		return ResponseEntity.ok(ApiResponseDto.ok("Plan updated", subscriptionPlanService.updatePlan(id, request)));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('subscription.plan.read')")
	public ResponseEntity<ApiResponseDto<PlanResponse>> getPlan(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponseDto.ok("OK", subscriptionPlanService.getPlan(id)));
	}

	@GetMapping
	@PreAuthorize("hasAuthority('subscription.plan.read')")
	public ResponseEntity<ApiResponseDto<List<PlanResponse>>> getPlans(@RequestParam(name = "activeOnly", required = false) Boolean activeOnly) {
		return ResponseEntity.ok(ApiResponseDto.ok("OK", subscriptionPlanService.getAllPlans(activeOnly)));
	}

	@PatchMapping("/{id}/activate")
	@PreAuthorize("hasAuthority('subscription.plan.manage')")
	public ResponseEntity<ApiResponseDto<Void>> activate(@PathVariable Long id) {
		subscriptionPlanService.activatePlan(id);
		return ResponseEntity.ok(ApiResponseDto.ok("Plan activated"));
	}

	@PatchMapping("/{id}/deactivate")
	@PreAuthorize("hasAuthority('subscription.plan.manage')")
	public ResponseEntity<ApiResponseDto<Void>> deactivate(@PathVariable Long id) {
		subscriptionPlanService.deactivatePlan(id);
		return ResponseEntity.ok(ApiResponseDto.ok("Plan deactivated"));
	}

	@PatchMapping("/{id}/default")
	@PreAuthorize("hasAuthority('subscription.plan.manage')")
	public ResponseEntity<ApiResponseDto<Void>> setDefault(@PathVariable Long id) {
		subscriptionPlanService.setDefaultPlan(id);
		return ResponseEntity.ok(ApiResponseDto.ok("Plan set as default"));
	}
}
