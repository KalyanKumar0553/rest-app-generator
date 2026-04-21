package com.src.main.auth.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import com.src.main.auth.dto.common.ApiResponseDto;
import com.src.main.auth.dto.request.ChangePasswordRequestDto;
import com.src.main.auth.dto.request.UpdateUserProfileRequestDto;
import com.src.main.auth.dto.response.UserSearchResponseDto;
import com.src.main.auth.dto.response.UserProfileResponseDto;
import com.src.main.auth.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserProfileController {

	private final AuthService authService;

	public UserProfileController(AuthService authService) {
		this.authService = authService;
	}

	@GetMapping("/profile")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponseDto<UserProfileResponseDto>> getProfile(org.springframework.security.core.Authentication auth) {
		return ResponseEntity.ok(ApiResponseDto.ok("OK", authService.getUserProfile(auth.getName())));
	}

	@PutMapping("/profile/update")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponseDto<UserProfileResponseDto>> updateProfile(
			org.springframework.security.core.Authentication auth,
			@RequestBody UpdateUserProfileRequestDto request) {
		return ResponseEntity.ok(ApiResponseDto.ok("Profile updated", authService.updateUserProfile(auth.getName(), request)));
	}

	@PostMapping("/password/change")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponseDto<Void>> changePassword(
			org.springframework.security.core.Authentication auth,
			@Valid @RequestBody ChangePasswordRequestDto request) {
		authService.changePassword(auth.getName(), request.getCurrentPassword(), request.getNewPassword());
		return ResponseEntity.ok(ApiResponseDto.ok("Password updated"));
	}

	@GetMapping("/search")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponseDto<List<UserSearchResponseDto>>> searchUsers(@RequestParam("query") String query) {
		return ResponseEntity.ok(ApiResponseDto.ok("OK", authService.searchUsers(query)));
	}
}
