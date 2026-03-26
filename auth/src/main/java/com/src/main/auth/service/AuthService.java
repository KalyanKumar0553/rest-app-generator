package com.src.main.auth.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.auth.dto.response.TokenPairResponseDto;
import com.src.main.auth.dto.response.AuthenticatedUserResponseDto;
import com.src.main.auth.dto.request.UpdateUserProfileRequestDto;
import com.src.main.auth.dto.response.UserSearchResponseDto;
import com.src.main.auth.dto.response.UserProfileResponseDto;
import com.src.main.auth.model.IdentifierType;
import com.src.main.auth.model.OtpPurpose;
import com.src.main.auth.model.OtpRequest;
import com.src.main.auth.model.RefreshToken;
import com.src.main.auth.model.Setting;
import com.src.main.auth.model.User;
import com.src.main.auth.model.UserProfile;
import com.src.main.auth.model.UserRole;
import com.src.main.auth.model.UserStatus;
import com.src.main.auth.repository.OtpRequestRepository;
import com.src.main.auth.repository.InvalidatedTokenRepository;
import com.src.main.auth.repository.RefreshTokenRepository;
import com.src.main.auth.repository.SettingRepository;
import com.src.main.auth.repository.UserProfileRepository;
import com.src.main.auth.repository.UserRepository;
import com.src.main.auth.repository.UserRoleRepository;
import com.src.main.auth.util.CryptoUtils;
import com.src.main.auth.util.IdentifierUtils;
import com.src.main.auth.util.JwtClaims;
import com.src.main.auth.util.JwtUtils;

@Service
public class AuthService {
	private final UserRepository userRepository;
	private final UserRoleRepository userRoleRepository;
	private final OtpRequestRepository otpRequestRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final InvalidatedTokenRepository invalidatedTokenRepository;
	private final SettingRepository settingRepository;
	private final UserProfileRepository userProfileRepository;
	private final JwtUtils jwtUtils;
	private final OtpSender otpSender;
	private final CaptchaService captchaService;
	private final RoleCatalogService roleCatalogService;
	private final RbacService rbacService;

	private final long accessTtl;
	private final long refreshTtl;
	private final long otpTtl;
	private final long otpCooldown;
	private final int otpDailyLimit;
	private final int maxFailed;
	private final long lockoutSeconds;

	public AuthService(
			UserRepository userRepository,
			UserRoleRepository userRoleRepository,
			OtpRequestRepository otpRequestRepository,
			RefreshTokenRepository refreshTokenRepository,
			InvalidatedTokenRepository invalidatedTokenRepository,
			SettingRepository settingRepository,
			UserProfileRepository userProfileRepository,
			JwtUtils jwtUtils,
			OtpSender otpSender,
			CaptchaService captchaService,
			RoleCatalogService roleCatalogService,
			RbacService rbacService,
			@Value("${jwt.access.ttl.seconds:900}") long accessTtl,
			@Value("${jwt.refresh.ttl.seconds:604800}") long refreshTtl,
			@Value("${otp.ttl.seconds:300}") long otpTtl,
			@Value("${otp.resend.cooldown.seconds:180}") long otpCooldown,
			@Value("${otp.daily.limit:5}") int otpDailyLimit,
			@Value("${security.max.failed.logins:5}") int maxFailed,
			@Value("${security.lockout.seconds:900}") long lockoutSeconds) {
		this.userRepository = userRepository;
		this.userRoleRepository = userRoleRepository;
		this.otpRequestRepository = otpRequestRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.invalidatedTokenRepository = invalidatedTokenRepository;
		this.settingRepository = settingRepository;
		this.userProfileRepository = userProfileRepository;
		this.jwtUtils = jwtUtils;
		this.otpSender = otpSender;
		this.captchaService = captchaService;
		this.roleCatalogService = roleCatalogService;
		this.rbacService = rbacService;
		this.accessTtl = accessTtl;
		this.refreshTtl = refreshTtl;
		this.otpTtl = otpTtl;
		this.otpCooldown = otpCooldown;
		this.otpDailyLimit = otpDailyLimit;
		this.maxFailed = maxFailed;
		this.lockoutSeconds = lockoutSeconds;
	}

	public boolean identifierExists(String identifier) {
		String id = IdentifierUtils.normalizeIdentifier(identifier);
		return findUserByNormalizedIdentifier(id).isPresent();
	}

	@Transactional
	public void signup(String identifier, String password, String captchaId, String captchaText) {
		captchaService.verify(captchaId, captchaText);
		String normalized = IdentifierUtils.normalizeIdentifier(identifier);
		IdentifierType type = IdentifierUtils.classify(normalized);
		User existing = findUserByNormalizedIdentifier(normalized).orElse(null);

		if (existing != null) {
			throw new IllegalStateException("Identifier already exists");
		}
		User user = new User();
		user.setIdentifier(normalized);
		user.setIdentifierType(type);
		user.setPasswordHash(CryptoUtils.hashPassword(password));
		user.setStatus(UserStatus.PENDING_VERIFICATION);
		userRepository.save(user);

		UserRole userRole = new UserRole();
		userRole.setUserId(user.getId());
		userRole.setRoleName(roleCatalogService.getDefaultAuthRoleName());
		userRoleRepository.save(userRole);

		generateOtpForUser(normalized, OtpPurpose.SIGNUP_VERIFICATION);
	}

	public CaptchaService.CaptchaResult generateCaptcha() {
		return captchaService.generate();
	}

	public void generateOtpForSignup(String identifier, String captchaId, String captchaText) {
		captchaService.verify(captchaId, captchaText);
		findUser(identifier);
		generateOtpForUser(identifier, OtpPurpose.SIGNUP_VERIFICATION);
	}

	@Transactional
	public void verifySignupOtp(String identifier, String otp) {
		User user = findUser(identifier);
		OtpRequest req = latestOtp(user.getId(), OtpPurpose.SIGNUP_VERIFICATION);
		if (req.getExpiresAt().isBefore(Instant.now())) {
			throw new IllegalArgumentException("OTP expired");
		}
		String expected = CryptoUtils.sha256Base64(otp, null);
		if (!expected.equals(req.getOtpHash())) {
			throw new IllegalArgumentException("Invalid OTP");
		}

		req.setUsed(true);
		otpRequestRepository.save(req);
		user.setStatus(UserStatus.ACTIVE);
		userRepository.save(user);
		revokeUserRefreshTokens(user.getId());
	}

	public void forgotPassword(String identifier, String captchaId, String captchaText) {
		captchaService.verify(captchaId, captchaText);
		findUser(identifier);
		generateOtpForUser(identifier, OtpPurpose.PASSWORD_RESET);
	}

	@Transactional
	public void resetPassword(String identifier, String otp, String newPassword) {
		User user = findUser(identifier);
		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new IllegalArgumentException("User is not active");
		}

		OtpRequest req = latestOtp(user.getId(), OtpPurpose.PASSWORD_RESET);
		if (req.getExpiresAt().isBefore(Instant.now())) {
			throw new IllegalArgumentException("OTP expired");
		}
		String expected = CryptoUtils.sha256Base64(otp, null);
		if (!expected.equals(req.getOtpHash())) {
			throw new IllegalArgumentException("Invalid OTP");
		}

		req.setUsed(true);
		otpRequestRepository.save(req);
		user.setPasswordHash(CryptoUtils.hashPassword(newPassword));
		userRepository.save(user);
		revokeUserRefreshTokens(user.getId());
	}

	@Transactional
	public void changePassword(String userId, String currentPassword, String newPassword) {
		User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new IllegalArgumentException("User is not active");
		}
		if (!CryptoUtils.verifyPassword(currentPassword, user.getPasswordHash())) {
			throw new IllegalArgumentException("Current password is incorrect");
		}
		user.setPasswordHash(CryptoUtils.hashPassword(newPassword));
		userRepository.save(user);
		revokeUserRefreshTokens(userId);
	}

	@Transactional(readOnly = true)
	public UserProfileResponseDto getUserProfile(String userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
		UserProfile profile = userProfileRepository.findById(userId).orElse(null);
		String firstName = profile == null || profile.getFirstName() == null ? null : profile.getFirstName().trim();
		String lastName = profile == null || profile.getLastName() == null ? null : profile.getLastName().trim();
		String name = buildDisplayName(user, profile);
		return new UserProfileResponseDto(
				user.getId(),
				user.getIdentifier(),
				name,
				firstName,
				lastName,
				profile == null ? null : profile.getAvatarUrl(),
				profile == null ? null : profile.getTimeZoneId());
	}

	@Transactional(readOnly = true)
	public List<UserSearchResponseDto> searchUsers(String query) {
		String normalizedQuery = query == null ? "" : query.trim();
		if (normalizedQuery.length() < 2) {
			return List.of();
		}
		java.util.LinkedHashMap<String, UserSearchResponseDto> results = new java.util.LinkedHashMap<>();
		findUserByNormalizedIdentifierSafe(normalizedQuery)
				.filter(user -> user.getStatus() == UserStatus.ACTIVE)
				.ifPresent(user -> results.put(user.getId(), toUserSearchResponse(user)));
		userRepository.searchActiveUsersByProfile(normalizedQuery, UserStatus.ACTIVE, PageRequest.of(0, 5))
				.stream()
				.sorted((left, right) -> Integer.compare(userSearchRank(left, normalizedQuery), userSearchRank(right, normalizedQuery)))
				.map(this::toUserSearchResponse)
				.forEach(dto -> results.putIfAbsent(dto.getUserId(), dto));
		return results.values().stream().limit(5).toList();
	}

	private int userSearchRank(User user, String query) {
		String normalizedQuery = query == null ? "" : query.trim().toLowerCase();
		UserProfile profile = userProfileRepository.findById(user.getId()).orElse(null);
		String firstName = profile == null || profile.getFirstName() == null ? "" : profile.getFirstName().trim().toLowerCase();
		String lastName = profile == null || profile.getLastName() == null ? "" : profile.getLastName().trim().toLowerCase();
		String fullName = (firstName + " " + lastName).trim();
		if (!normalizedQuery.isEmpty()) {
			if (fullName.equals(normalizedQuery)) {
				return 0;
			}
			if (firstName.equals(normalizedQuery)) {
				return 1;
			}
			if (lastName.equals(normalizedQuery)) {
				return 2;
			}
		}
		return 3;
	}

	@Transactional
	public UserProfileResponseDto updateUserProfile(String userId, UpdateUserProfileRequestDto request) {
		User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
		UserProfile profile = userProfileRepository.findById(userId).orElseGet(() -> {
			UserProfile created = new UserProfile();
			created.setUserId(userId);
			return created;
		});
		profile.setTimeZoneId(normalizeTimeZone(request == null ? null : request.getTimeZoneId()));
		userProfileRepository.save(profile);
		return getUserProfile(user.getId());
	}

	@Transactional
	public TokenPairResponseDto login(String identifier, String password) {
		String normalizedIdentifier = IdentifierUtils.normalizeIdentifier(identifier);
		User user = findUserByNormalizedIdentifier(normalizedIdentifier)
				.orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
		Instant now = Instant.now();
		if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
			throw new IllegalArgumentException("Account temporarily locked. Try later.");
		}
		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new IllegalArgumentException("User is not active");
		}

		boolean ok = CryptoUtils.verifyPassword(password, user.getPasswordHash());
		if (!ok) {
			int attempts = user.getFailedLoginAttempts() + 1;
			if (attempts >= maxFailed) {
				user.setLockedUntil(now.plusSeconds(lockoutSeconds));
				user.setFailedLoginAttempts(0);
			} else {
				user.setFailedLoginAttempts(attempts);
			}
			userRepository.save(user);
			throw new IllegalArgumentException("Invalid credentials");
		}

		user.setFailedLoginAttempts(0);
		user.setLockedUntil(null);
		userRepository.save(user);
		return issueTokens(user.getId());
	}

	public TokenPairResponseDto loginWithUserId(String userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new IllegalArgumentException("User is not active");
		}
		return issueTokens(user.getId());
	}

	@Transactional
	public TokenPairResponseDto refresh(String refreshTokenJwt) {
		JwtClaims claims = jwtUtils.parse(refreshTokenJwt);
		if (!"refresh".equals(claims.getTyp()) || claims.getRid() == null) {
			throw new IllegalArgumentException("Not a refresh token");
		}

		RefreshToken token = refreshTokenRepository
				.findFirstByIdAndRevokedFalseAndExpiresAtAfter(claims.getRid(), Instant.now())
				.orElse(null);
		if (token == null) {
			RefreshToken any = refreshTokenRepository.findById(claims.getRid()).orElse(null);
			if (any != null) {
				revokeFamily(any.getFamilyId());
			}
			throw new IllegalArgumentException("Refresh token revoked or expired");
		}

		token.setRevoked(true);
		refreshTokenRepository.save(token);

		User user = userRepository.findById(claims.getSub()).orElseThrow(() -> new IllegalArgumentException("User not found"));
		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new IllegalArgumentException("User is not active");
		}
		return issueTokens(user.getId(), token.getFamilyId());
	}

	@Transactional
	public void logout(String refreshTokenJwt, String accessToken) {
		JwtClaims claims = jwtUtils.parse(refreshTokenJwt);
		if (!"refresh".equals(claims.getTyp()) || claims.getRid() == null) {
			throw new IllegalArgumentException("Not a refresh token");
		}
		RefreshToken token = refreshTokenRepository.findById(claims.getRid()).orElse(null);
		if (token != null) {
			token.setRevoked(true);
			refreshTokenRepository.save(token);
		}
		invalidateAccessToken(accessToken);
	}

	public void invalidateAccessToken(String accessToken) {
		if (accessToken == null || accessToken.isBlank()) {
			return;
		}
		String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
		try {
			JwtClaims claims = jwtUtils.parse(token);
			if (!"access".equals(claims.getTyp())) {
				return;
			}
			if (invalidatedTokenRepository.existsByToken(token)) {
				return;
			}
			com.src.main.auth.model.InvalidatedToken invalidated = new com.src.main.auth.model.InvalidatedToken();
			invalidated.setToken(token);
			invalidated.setExpiresAt(jwtUtils.getExpiration(token));
			invalidatedTokenRepository.save(invalidated);
		} catch (Exception ignored) {
			// Ignore invalid/expired tokens during logout
		}
	}

	public boolean validate(String token) {
		try {
			jwtUtils.parse(token);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	@Transactional
	public void updateSwaggerPassword(String username, String password) {
		String hash = CryptoUtils.hashPassword(password);
		Setting setting = settingRepository.findFirstBySourceAndUsername("swagger", username).orElse(null);
		if (setting == null) {
			setting = new Setting();
			setting.setSource("swagger");
			setting.setUsername(username);
		}
		setting.setHash(hash);
		setting.setPassword(null);
		setting.setSalt(null);
		settingRepository.save(setting);
	}

	public String issueSwaggerToken(String username, String password) {
		Setting setting = settingRepository.findFirstBySourceAndUsername("swagger", username).orElse(null);
		if (setting == null || setting.getHash() == null) {
			if (!"swagger".equals(username) || !"swagger1234".equals(password)) {
				throw new IllegalArgumentException("Invalid swagger credentials");
			}
			return jwtUtils.signAccess("swagger:" + username, List.of("ROLE_SWAGGER_ADMIN"), List.of("swagger.password.manage"), accessTtl);
		}
		if (!CryptoUtils.verifyPassword(password, setting.getHash())) {
			throw new IllegalArgumentException("Invalid swagger credentials");
		}
		return jwtUtils.signAccess("swagger:" + username, List.of("ROLE_SWAGGER_ADMIN"), List.of("swagger.password.manage"), accessTtl);
	}

	public List<String> getUserRoles(String userId) {
		return rbacService.getAccessProfile(userId).roles();
	}

	public List<String> getUserPermissions(String userId) {
		return rbacService.getAccessProfile(userId).permissions();
	}

	private void generateOtpForUser(String identifier, OtpPurpose purpose) {
		User user = findUser(identifier);
		Instant start = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
		Instant end = start.plusSeconds(24 * 60 * 60 - 1);
		long sentToday = otpRequestRepository.countByUserIdAndCreatedAtBetween(user.getId(), start, end);
		if (sentToday >= otpDailyLimit) {
			throw new IllegalArgumentException("Daily OTP limit exceeded (" + otpDailyLimit + ")");
		}

		OtpRequest latest = otpRequestRepository
				.findFirstByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(user.getId(), purpose)
				.orElse(null);
		if (latest != null) {
			long secondsSince = Instant.now().getEpochSecond() - latest.getCreatedAt().getEpochSecond();
			if (secondsSince < otpCooldown) {
				throw new IllegalArgumentException("Please wait " + otpCooldown + " seconds between OTP requests");
			}
		}

		String otp = CryptoUtils.generateOtp6();
		OtpRequest request = new OtpRequest();
		request.setUserId(user.getId());
		request.setPurpose(purpose);
		request.setOtpHash(CryptoUtils.sha256Base64(otp, null));
		request.setExpiresAt(Instant.now().plusSeconds(otpTtl));
		request.setUsed(false);
		otpRequestRepository.save(request);

		otpSender.send(user.getIdentifierType(), user.getIdentifier(), otp);
	}

	private OtpRequest latestOtp(String userId, OtpPurpose purpose) {
		return otpRequestRepository
				.findFirstByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(userId, purpose)
				.orElseThrow(() -> new IllegalArgumentException("No valid OTP found. Please request a new OTP."));
	}

	private TokenPairResponseDto issueTokens(String userId) {
		return issueTokens(userId, CryptoUtils.uuid());
	}

	private TokenPairResponseDto issueTokens(String userId, String familyId) {
		RbacService.AccessProfile accessProfile = rbacService.getAccessProfile(userId);
		List<String> roles = accessProfile.roles();
		List<String> permissions = accessProfile.permissions();
		String access = jwtUtils.signAccess(userId, roles, permissions, accessTtl);
		String refreshId = CryptoUtils.uuid();
		String refresh = jwtUtils.signRefresh(userId, refreshId, refreshTtl);
		
		RefreshToken token = new RefreshToken();
		token.setId(refreshId);
		token.setUserId(userId);
		token.setFamilyId(familyId);
		token.setTokenHash(CryptoUtils.sha256Base64(refresh, null));
		token.setExpiresAt(Instant.now().plusSeconds(refreshTtl));
		token.setRevoked(false);
		refreshTokenRepository.save(token);

		return new TokenPairResponseDto(access, refresh, buildAuthenticatedUser(userId, roles, permissions));
	}

	private void revokeUserRefreshTokens(String userId) {
		List<RefreshToken> tokens = refreshTokenRepository.findByUserId(userId);
		for (RefreshToken token : tokens) {
			token.setRevoked(true);
		}
		refreshTokenRepository.saveAll(tokens);
	}

	private void revokeFamily(String familyId) {
		List<RefreshToken> tokens = refreshTokenRepository.findByFamilyId(familyId);
		for (RefreshToken token : tokens) {
			token.setRevoked(true);
		}
		refreshTokenRepository.saveAll(tokens);
	}

	private User findUser(String identifier) {
		String id = IdentifierUtils.normalizeIdentifier(identifier);
		return findUserByNormalizedIdentifier(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
	}

	private java.util.Optional<User> findUserByNormalizedIdentifier(String normalizedIdentifier) {
		return userRepository.findByIdentifier(normalizedIdentifier);
	}

	private java.util.Optional<User> findUserByNormalizedIdentifierSafe(String identifier) {
		try {
			String normalized = IdentifierUtils.normalizeIdentifier(identifier);
			return findUserByNormalizedIdentifier(normalized);
		} catch (IllegalArgumentException ex) {
			return java.util.Optional.empty();
		}
	}

	private UserSearchResponseDto toUserSearchResponse(User user) {
		UserProfile profile = userProfileRepository.findById(user.getId()).orElse(null);
		String identifier = user.getIdentifier();
		String displayName = buildDisplayName(user, profile);
		return new UserSearchResponseDto(
				user.getId(),
				displayName,
				identifier,
				profile == null ? null : profile.getAvatarUrl());
	}

	private AuthenticatedUserResponseDto buildAuthenticatedUser(String userId, List<String> roles, List<String> permissions) {
		User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
		var profile = userProfileRepository.findById(userId).orElse(null);
		String name = buildDisplayName(user, profile);
		String avatarUrl = null;

		if (profile != null) {
			avatarUrl = profile.getAvatarUrl();
		}

		String primaryRole = roles.isEmpty() ? roleCatalogService.getDefaultAuthRoleName() : roles.get(0);
		return new AuthenticatedUserResponseDto(user.getId(), user.getIdentifier(), name, primaryRole, roles, permissions, avatarUrl);
	}

	private String buildDisplayName(User user, UserProfile profile) {
		String name = user.getIdentifier();
		if (profile == null) {
			return name;
		}
		String firstName = profile.getFirstName() == null ? "" : profile.getFirstName().trim();
		String lastName = profile.getLastName() == null ? "" : profile.getLastName().trim();
		String fullName = (firstName + " " + lastName).trim();
		return fullName.isEmpty() ? name : fullName;
	}

	private String normalizeTimeZone(String timeZoneId) {
		if (timeZoneId == null || timeZoneId.isBlank()) {
			return null;
		}
		String trimmed = timeZoneId.trim();
		try {
			ZoneId.of(trimmed);
			return trimmed;
		} catch (Exception ex) {
			throw new IllegalArgumentException("Invalid time zone");
		}
	}
}
