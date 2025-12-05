package com.src.main.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.src.main.dto.JSONResponseDTO;
import com.src.main.dto.LoginRequestDTO;
import com.src.main.dto.OTPVerificationRequestDTO;
import com.src.main.dto.ResetPasswordRequestDTO;
import com.src.main.dto.SendOTPRequestDTO;
import com.src.main.dto.SignupRequestDTO;
import com.src.main.service.AuthService;
import com.src.main.util.AppUtils;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@AllArgsConstructor
@CrossOrigin
@RequestMapping("/api/auth")
public class AuthController {

	final AuthService authService;

	@PostMapping("/signup")
	public ResponseEntity<JSONResponseDTO<?>> registerUser(@RequestBody @Valid SignupRequestDTO signupRequest)
			throws MessagingException {
		return authService.signup(signupRequest);
	}

	@PostMapping("/login")
	public ResponseEntity<JSONResponseDTO<?>> authenticateUser(@RequestBody @Valid LoginRequestDTO loginRequest,
			HttpServletResponse response) {
		return authService.login(loginRequest, response);
	}

	@PostMapping("/verify-otp")
	public ResponseEntity<JSONResponseDTO<?>> verifyOtp(@RequestBody @Valid OTPVerificationRequestDTO otpVerificationRequest) {
		return authService.verifyOTP(otpVerificationRequest);
	}

	@PostMapping("/send-otp")
	public ResponseEntity<JSONResponseDTO<?>> sendOtp(@RequestBody @Valid SendOTPRequestDTO sendOTPRequest) {
		return authService.sendOTP(sendOTPRequest);
	}

	@GetMapping("/roles")
	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','USER','AGENT')")
	public ResponseEntity<JSONResponseDTO<?>> getRoles(Authentication authentication) throws MessagingException {
		return ResponseEntity.ok(AppUtils.getJSONObject(authentication.getAuthorities()));
	}

	@PostMapping("/reset-password-with-otp")
	public ResponseEntity<JSONResponseDTO<?>> resetPasswordWithOTP(
			@RequestBody @Valid ResetPasswordRequestDTO resetPasswordRequest, Authentication authentication) {
		return authService.resetPasswordWithOTP(authentication,resetPasswordRequest);
	}

	@PostMapping("/refresh-token")
	public ResponseEntity<JSONResponseDTO<?>> refreshAccessToken(
			@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
		return authService.refreshToken(refreshToken,response);
	}

	@PostMapping("/logout")
	public ResponseEntity<JSONResponseDTO<?>> logout(@RequestHeader("Authorization") String accessToken, @CookieValue(name = "refreshToken", required = false) String refreshToken,HttpServletResponse response) {
		return authService.logout(accessToken,refreshToken,response);
	}
}