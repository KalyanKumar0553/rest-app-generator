package com.src.main.validators;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.src.main.dto.UserProfileRequestDTO;
import com.src.main.exception.AbstractRuntimeException;
import com.src.main.model.UserInfo;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class UserValidator {

	public void validateUserProfileRequest(UserProfileRequestDTO profileRequest,UserInfo user) {
		Optional<String> email = Optional.ofNullable(profileRequest.getEmail());
		Optional<String> mobile = Optional.ofNullable(profileRequest.getMobile());
		Optional<String> fullName = Optional.ofNullable(profileRequest.getFullName());
		if(user.getEmail()!=null) {
			if (email.isEmpty() || email.get().isEmpty()) {
				throw new AbstractRuntimeException(500,"Email Cannot Be Blank");
			}
			if(!user.getEmail().equals(email.get()) || user.getUsername().equals(email.get())) {
				throw new AbstractRuntimeException(500,"User cannot update registered email");
			}
		}

		if(user.getMobile()!=null) {
			if (mobile.isEmpty() || mobile.get().isEmpty()) {
				throw new AbstractRuntimeException(500,"Mobile Cannot Be Blank");
			}
			if(!user.getMobile().equals(mobile.get()) || user.getUsername().equals(mobile.get())) {
				throw new AbstractRuntimeException(500,"User cannot update registered mobile");
			}
		}
		user.setFullName(profileRequest.getFullName());
		user.setEmail(profileRequest.getEmail());
		user.setMobile(profileRequest.getMobile());
	}
}
