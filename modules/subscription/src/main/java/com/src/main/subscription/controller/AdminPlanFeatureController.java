package com.src.main.subscription.controller;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.src.main.auth.dto.common.ApiResponseDto;
import com.src.main.subscription.dto.PlanFeatureResponse;
import com.src.main.subscription.dto.PlanFeatureUpsertRequest;
import com.src.main.subscription.service.PlanFeatureService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/subscription/plans/{planId}/features")
@ConditionalOnProperty(prefix = "app.subscription", name = "admin-apis-enabled", havingValue = "true", matchIfMissing = true)
public class AdminPlanFeatureController {
	private final PlanFeatureService planFeatureService;

	@GetMapping
	@PreAuthorize("hasAuthority(\'subscription.plan.read\')")
	public ResponseEntity<ApiResponseDto<List<PlanFeatureResponse>>> getPlanFeatures(@PathVariable Long planId) {
		return ResponseEntity.ok(ApiResponseDto.ok("OK", planFeatureService.getPlanFeatures(planId)));
	}

	@PutMapping
	@PreAuthorize("hasAuthority(\'subscription.feature.manage\')")
	public ResponseEntity<ApiResponseDto<List<PlanFeatureResponse>>> replaceFeatures(@PathVariable Long planId, @RequestBody List<@Valid PlanFeatureUpsertRequest> requests) {
		return ResponseEntity.ok(ApiResponseDto.ok("Plan features replaced", planFeatureService.replacePlanFeatures(planId, requests)));
	}

	@PutMapping("/{featureId}")
	@PreAuthorize("hasAuthority(\'subscription.feature.manage\')")
	public ResponseEntity<ApiResponseDto<PlanFeatureResponse>> upsertFeature(@PathVariable Long planId, @PathVariable Long featureId, @Valid @RequestBody PlanFeatureUpsertRequest request) {
		return ResponseEntity.ok(ApiResponseDto.ok("Plan feature updated", planFeatureService.upsertPlanFeature(planId, request)));
	}

	@DeleteMapping("/{featureId}")
	@PreAuthorize("hasAuthority(\'subscription.feature.manage\')")
	public ResponseEntity<ApiResponseDto<Void>> removeFeature(@PathVariable Long planId, @PathVariable Long featureId) {
		planFeatureService.removePlanFeature(planId, featureId);
		return ResponseEntity.ok(ApiResponseDto.ok("Plan feature removed"));
	}

	public AdminPlanFeatureController(final PlanFeatureService planFeatureService) {
		this.planFeatureService = planFeatureService;
	}
}
