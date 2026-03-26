package com.src.main.subscription.controller;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
import com.src.main.subscription.dto.FeatureRequest;
import com.src.main.subscription.dto.FeatureResponse;
import com.src.main.subscription.service.SubscriptionFeatureService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/subscription/features")
@ConditionalOnProperty(prefix = "app.subscription", name = "admin-apis-enabled", havingValue = "true", matchIfMissing = true)
public class AdminSubscriptionFeatureController {
	private final SubscriptionFeatureService subscriptionFeatureService;

	@PostMapping
	@PreAuthorize("hasAuthority(\'subscription.feature.manage\')")
	public ResponseEntity<ApiResponseDto<FeatureResponse>> create(@Valid @RequestBody FeatureRequest request) {
		return ResponseEntity.ok(ApiResponseDto.ok("Feature created", subscriptionFeatureService.createFeature(request)));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority(\'subscription.feature.manage\')")
	public ResponseEntity<ApiResponseDto<FeatureResponse>> update(@PathVariable Long id, @Valid @RequestBody FeatureRequest request) {
		return ResponseEntity.ok(ApiResponseDto.ok("Feature updated", subscriptionFeatureService.updateFeature(id, request)));
	}

	@GetMapping
	@PreAuthorize("hasAuthority(\'subscription.feature.read\')")
	public ResponseEntity<ApiResponseDto<List<FeatureResponse>>> list(@RequestParam(name = "activeOnly", required = false) Boolean activeOnly) {
		return ResponseEntity.ok(ApiResponseDto.ok("OK", subscriptionFeatureService.getAllFeatures(activeOnly)));
	}

	@GetMapping("/{code}")
	@PreAuthorize("hasAuthority(\'subscription.feature.read\')")
	public ResponseEntity<ApiResponseDto<FeatureResponse>> getByCode(@PathVariable String code) {
		return ResponseEntity.ok(ApiResponseDto.ok("OK", subscriptionFeatureService.getFeatureByCode(code)));
	}

	public AdminSubscriptionFeatureController(final SubscriptionFeatureService subscriptionFeatureService) {
		this.subscriptionFeatureService = subscriptionFeatureService;
	}
}
