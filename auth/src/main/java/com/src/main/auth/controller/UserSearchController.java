package com.src.main.auth.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.src.main.auth.dto.common.ApiResponseDto;
import com.src.main.auth.dto.response.UserSearchResponseDto;
import com.src.main.auth.service.AuthService;

@RestController
@RequestMapping("/api/users")
public class UserSearchController {
	private final AuthService authService;

	@GetMapping("/search")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponseDto<List<UserSearchResponseDto>>> searchUsers(@RequestParam("query") String query) {
		return ResponseEntity.ok(ApiResponseDto.ok("OK", authService.searchUsers(query)));
	}

	public UserSearchController(final AuthService authService) {
		this.authService = authService;
	}
}
