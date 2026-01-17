package com.src.main.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.src.main.auth.controller.AdminAuthController;
import com.src.main.auth.dto.common.ApiResponseDto;
import com.src.main.auth.dto.request.LoginRequestDto;
import com.src.main.auth.dto.response.TokenPairResponseDto;
import com.src.main.auth.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/auth")
@ConditionalOnMissingBean(AdminAuthController.class)
public class AdminAuthProxyController {
	private final AuthService authService;

	public AdminAuthProxyController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponseDto<TokenPairResponseDto>> login(@RequestBody @Valid LoginRequestDto dto) {
		return ResponseEntity.ok(ApiResponseDto.ok("Authenticated", authService.login(dto.getIdentifier(), dto.getPassword())));
	}
}
