package com.src.main.service;

import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;

import com.src.main.config.JWTTokenProvider;
import com.src.main.dto.JSONResponseDTO;
import com.src.main.dto.LoginRequestDTO;
import com.src.main.dto.OTPVerificationRequestDTO;
import com.src.main.dto.ResetPasswordRequestDTO;
import com.src.main.dto.SendOTPRequestDTO;
import com.src.main.dto.SignupRequestDTO;
import com.src.main.exception.UserNotFoundException;
import com.src.main.model.UserInfo;
import com.src.main.transformers.UserInfoTransformer;
import com.src.main.util.AppUtils;
import com.src.main.util.PasswordUtil;
import com.src.main.util.RequestStatus;
import com.src.main.validators.AuthValidator;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthService {

	final UserInfoTransformer userInfoTransformer;

	final JWTTokenProvider jwtTokenProvider;

	final AuthenticationManager authenticationManager;

	final JWTTokenProvider tokenProvider;

	final UserDetailsServiceImpl userService;

	final PasswordEncoder passwordEncoder;

	final AuthValidator authValidator;

	public ResponseEntity<JSONResponseDTO<?>> login(LoginRequestDTO loginRequest, HttpServletResponse response) {
		UserInfo user = userService.validateAndGetIfUserEnabled(loginRequest);
		String hashPassword = PasswordUtil.hashPassword(loginRequest.getPassword(), user.getSalt());
		String encodedPassword = passwordEncoder.encode(hashPassword);
		if (!passwordEncoder.matches(hashPassword, encodedPassword)) {
			throw new UserNotFoundException(RequestStatus.BAD_CREDENTIALS);
		}
		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user.getUsername(),
				hashPassword, userService.getAuthorities(user.getUuid()));
		Authentication authentication = authenticationManager.authenticate(authToken);

		String accessToken = tokenProvider.generateAccessToken(authentication, user.getUuid());
		String refreshToken = tokenProvider.generateRefreshToken(user.getUsername(), user.getUuid());

		ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken).httpOnly(true).secure(true)
				.sameSite("Strict").path("/api/auth/refresh-token").maxAge(Duration.ofDays(7)).build();

		response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
		response.addHeader("Authorization", "Bearer " + accessToken);
		SecurityContextHolder.getContext().setAuthentication(authToken);

		Date expiresAt = tokenProvider.extractExpiration(accessToken);
		DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		String formatted = expiresAt.toInstant().atZone(ZoneId.systemDefault()).format(formatter);

		return ResponseEntity.ok(AppUtils.getJSONObject(Map.of("accessToken", accessToken, "expiresAt", formatted)));

	}

	public ResponseEntity<JSONResponseDTO<?>> signup(SignupRequestDTO signupRequest) {
		authValidator.validateSignUpRequest(signupRequest);
		userService.checkDuplicateUser(signupRequest);
		String msgSource = authValidator.isMobileNumberProivded(signupRequest) ? "mobile" : "email";
		UserInfo userInfo = userInfoTransformer.fromSignupRequestDTO(signupRequest);
		userService.saveUser(userInfo, passwordEncoder);
		String msg = String.format(RequestStatus.SIGNUP_SUCCESS.getDescription(msgSource));
		return ResponseEntity.ok(AppUtils.getJSONObject(msg));
	}

	public ResponseEntity<JSONResponseDTO<?>> verifyOTP(OTPVerificationRequestDTO otpVerificationRequest) {
		 authValidator.validateOTPVerificationRequest(otpVerificationRequest);
		UserInfo user = userService.findUserByUsername(otpVerificationRequest.getUsername())
				.orElseThrow(() -> new UserNotFoundException(RequestStatus.USER_NOT_FOUND));
		if (user.isEnabled()) {
			return ResponseEntity
					.ok(AppUtils.getJSONObject(RequestStatus.USER_ALREADY_VERIFIED_ERROR.getDescription()));
		}
		String msg = userService.verifyOtp(otpVerificationRequest.getUsername(), otpVerificationRequest.getOtp());
		return ResponseEntity.ok(AppUtils.getJSONObject(msg));

	}

	public ResponseEntity<JSONResponseDTO<?>> sendOTP(SendOTPRequestDTO sendOTPRequest) {
		authValidator.validateSendOTPRequest(sendOTPRequest);
		String username = sendOTPRequest.getUsername();
		UserInfo user = userService.findUserByUsername(username)
				.orElseThrow(() -> new UserNotFoundException(RequestStatus.USER_NOT_FOUND));
		Context context = new Context();
		context.setVariable("otp", userService.generateOtp());
		boolean otpSent = userService.sendOTP(user, context);
		return ResponseEntity.ok(AppUtils.getJSONObject(otpSent ? RequestStatus.OTP_SENT_SUCCESS.getDescription()
				: RequestStatus.OTP_SENT_FAIL.getDescription()));
	}

	public ResponseEntity<JSONResponseDTO<?>> resetPasswordWithOTP(Authentication authentication,
			ResetPasswordRequestDTO resetPasswordRequest) {
		authValidator.validateResetPasswordWithOTPRequest(resetPasswordRequest);
		UserInfo user = userService.findUserByUsername(resetPasswordRequest.getUsername())
				.orElseThrow(() -> new UserNotFoundException(RequestStatus.USER_NOT_FOUND));
		userService.resetPasswordWithOTP(user, resetPasswordRequest.getOtp(), resetPasswordRequest.getPassword(),
				passwordEncoder);
		return ResponseEntity.ok(AppUtils.getJSONObject(RequestStatus.PASSWORD_RESET_SUCCESS.getDescription()));
	}

	public ResponseEntity<JSONResponseDTO<?>> logout(String accessToken, String refreshToken,
			HttpServletResponse response) {
		if (refreshToken != null) {
			jwtTokenProvider.invalidateRefreshToken(refreshToken);
		}
		if (StringUtils.hasText(accessToken) && accessToken.startsWith("Bearer ")) {
			accessToken = accessToken.substring(7);
			jwtTokenProvider.invalidateToken(accessToken);
		}
		ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "").httpOnly(true).secure(true)
				.path("/auth/refresh-token").maxAge(0).build();
		response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());
		return ResponseEntity.ok(AppUtils.getJSONObject("Logged out successfully"));

	}

	public ResponseEntity<JSONResponseDTO<?>> refreshToken(String refreshToken, HttpServletResponse response) {
		if (refreshToken == null || !tokenProvider.validateToken(refreshToken)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
					JSONResponseDTO.builder().isError(false).statusMsg("Invalid or expired refresh token").build());
		}

		String username = tokenProvider.getUsernameFromToken(refreshToken);
		String userUUID = tokenProvider.getUserIDFromToken(refreshToken);

		UserDetails userDetails = userService.loadUserByUsername(username);

		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String newAccessToken = tokenProvider.generateAccessToken(authentication, userUUID);
		String newRefreshToken = tokenProvider.generateRefreshToken(username, userUUID);
		Date expiresAt = tokenProvider.extractExpiration(newAccessToken);

		ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefreshToken).httpOnly(true).secure(true)
				.sameSite("Strict").path("/api/auth/refresh-token").maxAge(Duration.ofDays(7)).build();

		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
		response.addHeader("Authorization", "Bearer " + newAccessToken);

		// Format expiresAt to ISO_OFFSET_DATE_TIME
		DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		String formatted = expiresAt.toInstant().atZone(ZoneId.systemDefault()).format(formatter);

		JSONResponseDTO refreshTokenResponse = JSONResponseDTO.builder().isError(false)
				.statusMsg(Map.of("accessToken", newAccessToken, "expiresAt", formatted)).build();

		return ResponseEntity.ok(refreshTokenResponse);
	}

}
