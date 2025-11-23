package com.src.main.validators;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;

import com.src.main.exception.AbstractRuntimeException;
import com.src.main.util.AppConstants;

public class AppValidators {

	public static boolean isMobile(String input) {
		return (!isEmpty(input)) && Pattern.matches(AppConstants.mobileRegex, input);
	}

	public static boolean isEmail(String input) {
		return (!isEmpty(input)) && Pattern.matches(AppConstants.emailRegex, input);
	}

	private static boolean isEmpty(String input) {
		Optional<String> nullableIinput = Optional.ofNullable(input);
		if(nullableIinput.isEmpty()) {
			return true;
		}
		if(input.isEmpty() || input.isBlank()) {
			return true;
		}
		return false;
	}
	
	public static void validatePassword(String password) {
		if (password.length() < 8) {
            throw new AbstractRuntimeException(HttpStatus.BAD_REQUEST.value(),"Password must be at least 8 characters long.");
        }
		if (password.length() > 30) {
            throw new AbstractRuntimeException(HttpStatus.BAD_REQUEST.value(),"Password should have maximum Length of 30");
        }
        if (!password.matches(".*[A-Z].*")) {
        	throw new AbstractRuntimeException(HttpStatus.BAD_REQUEST.value(),"Password must contain at least one uppercase letter.");
        }
        if (!password.matches(".*[0-9].*")) {
        	throw new AbstractRuntimeException(HttpStatus.BAD_REQUEST.value(),"Password must contain at least one digit.");
        }
        if (!password.matches(".*[a-z].*")) {
        	throw new AbstractRuntimeException(HttpStatus.BAD_REQUEST.value(),"Password must contain at least one lowercase letter.");
        }
        if (!password.matches(".*[@#$%*()_].*")) {
        	throw new AbstractRuntimeException(HttpStatus.BAD_REQUEST.value(),"Password must contain at least one special character @ # $ % * ( ) _ ");
        }
        if (!password.matches("^[A-Za-z0-9@#$%*()_]+$")) {
        	throw new AbstractRuntimeException(HttpStatus.BAD_REQUEST.value(),"Password contains invalid characters. Only letters, digits, and @ # $ % * ( ) _ are allowed.");
        }

	}
}
