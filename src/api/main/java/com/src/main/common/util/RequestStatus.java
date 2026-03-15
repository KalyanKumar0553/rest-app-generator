package com.src.main.common.util;

import org.springframework.http.HttpStatus;

public enum RequestStatus {

	USER_NOT_FOUND(HttpStatus.NOT_FOUND.value(),"User not found"),
	ROLE_SUCCESS(HttpStatus.OK.value(),"Roles for user %s successfully fetched"),
	PASSWORD_RESET_SUCCESS(HttpStatus.OK.value(), "Password updated successfully. Please log in with your new credentials."),
	LOGIN_SUCCESS(HttpStatus.OK.value(), "Login successful"),
	LOGOUT_SUCCESS(HttpStatus.OK.value(), "Logged out successfully"),
	SIGNUP_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Unable To Create the User"),
	SIGNUP_SUCCESS(HttpStatus.OK.value(), "User registered successfully. Please verify OTP sent to %s "),
	LOGIN_REQUEST_PASSWORD_REQUIRED_ERROR(HttpStatus.BAD_REQUEST.value(),"Invalid Request. Password is Required to login"),
	LOGIN_REQUEST_USERNAME_MAX_LENGTH_ERROR(HttpStatus.BAD_REQUEST.value(),"Username Should have Maximum length of 30"),
	LOGIN_REQUEST_INVALID_USERNAME_ERROR(HttpStatus.BAD_REQUEST.value(),"Invalid Username. Please enter valid email or 10 digit mobile number."),
	LOGIN_REQUEST_USERNAME_REQUIRED_ERROR(HttpStatus.BAD_REQUEST.value(),"Invalid Request. Username is Required to login"),
	OTP_VERIFICATION_ERROR_OTP_REQUIRED(HttpStatus.BAD_REQUEST.value(),"Invalid Request. OTP is Required to verify"),
	VERIFICATION_REQUEST_OTP_ERROR(HttpStatus.BAD_REQUEST.value(),"Invalid Request. Please enter valid OTP"),
	SIGNUP_REQUEST_DATA_ERROR(HttpStatus.BAD_REQUEST.value(),"Invalid Request to Fetch Mobile / Email"),
	SIGNUP_REQUEST_EMAIL_DATA_ERROR(HttpStatus.BAD_REQUEST.value(),"Need Email To Register the account"),
	SIGNUP_REQUEST_MOBILE_DATA_ERROR(HttpStatus.BAD_REQUEST.value(),"Need Mobile To Register the account"),
	BAD_CREDENTIALS(HttpStatus.NOT_FOUND.value(),"Invalid Credentials"),
	USER_DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST.value(),"User with email already taken. Please try with different email"),
	USER_DUPLICATE_MOBILE(HttpStatus.BAD_REQUEST.value(),"User with mobile already taken. Please try with different mobile"),
	EMAIL_SENT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Unable To Send OTP"),
	USER_ALREADY_VERIFIED_ERROR(HttpStatus.OK.value(), "User already exists and verified. Please try to reset password"),
	OTP_VERIFICATION_ERROR_USERNAME_REQUIRED_ERROR(HttpStatus.BAD_REQUEST.value(),"Invalid Request. Username is Required to verify OTP."),
	OTP_NOT_VERIFIED_ERROR(HttpStatus.BAD_REQUEST.value(),"User not verified OTP. Please verify OTP Sent to %s or use forgot password"),
	OTP_SENT_SUCCESS(HttpStatus.OK.value(), "OTP has been sent to your email to verify."),
	OTP_SENT_FAIL(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unable to send OTP"),
	OTP_LIMIT_EXCEED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "OTP Limit Has been reached for today. Please try again tomorrow."),
	OTP_TIME_LIMIT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "OTP Has been already sent. Please try after %s"),
	OTP_EXPIRATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "OTP Expired. Please enter latest OTP"),
	OTP_VERIFICATION_FAIL(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Unable to verify OTP. Please try again"),
	OTP_VERIFICATION_SUCCESS(HttpStatus.OK.value(),"OTP Verified succesfully"),
	SENDOTP_ERROR_OTP_REQUIRED_ERROR(HttpStatus.BAD_REQUEST.value(),"Invalid Request. OTP is Required to Send OTP."),
	SENDOTP_ERROR_USERNAME_REQUIRED_ERROR(HttpStatus.BAD_REQUEST.value(),"Invalid Request. Username is Required to Send OTP."),
	RESET_PASSWORD_ERROR_USERNAME_REQUIRED(HttpStatus.BAD_REQUEST.value(),"Invalid Request. Username is Required to Reset Password."),
	RESET_PASSWORD_ERROR_PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST.value(),"Invalid Request. Password is Required to Reset Password."),
	RESET_PASSWORD_ERROR_OTP_REQUIRED(HttpStatus.BAD_REQUEST.value(),"Invalid Request. OTP is Required to Reset Password."),
	RESET_PASSWORD_ERROR_RETYPE_PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST.value(),"Invalid Request. Retype Password is Required."),
	RESET_PASSWORD_ERROR(HttpStatus.BAD_REQUEST.value(),"Error: Failed To Reset Password"),
	RESET_PASSWORD_ERROR_PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST.value(),"Invalid Request. Password and Retype Doesn't match"),
	
	PROJECT_FETCH_SUCCESS(HttpStatus.OK.value(),"Projects retrieved successfully"),
	PROJECT_FETCH_FAIL(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Unable To Fetch Project Details"),
	
	UPLOAD_FILE_SUCCESS(HttpStatus.OK.value(),"Success: Data Succesfully Uploaded"),
	UPLOAD_FILE_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Error: Failed To Upload Data");


	
	private final int code;
	private final String description;

	private RequestStatus(int code, String description) {
		this.code = code;
		this.description = description;
	}

	public String getDescription(Object... params) {
		return String.format(description, params);
	}

	public int getCode() {
		return code;
	}

	@Override
	public String toString() {
		return code + ": " + description;
	}
}
