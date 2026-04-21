package com.src.main.auth.config;

public final class AuthDbTables {
	public static final String USERS = "users";
	public static final String ROLES = "roles";
	public static final String PERMISSIONS = "permissions";
	public static final String ROLE_PERMISSIONS = "role_permissions";
	public static final String USER_ROLES = "user_roles";
	public static final String OTP_REQUESTS = "otp_requests";
	public static final String REFRESH_TOKENS = "refresh_tokens";
	public static final String CAPTCHA_CHALLENGES = "captcha_challenges";
	public static final String SETTINGS = "settings";
	public static final String USER_PROFILES = "user_profiles";
	public static final String INVALIDATED_TOKENS = "invalidated_tokens";
	public static final String ROUTES = "routes";

	private AuthDbTables() {
	}
}
