package com.src.main.transformers;

import org.springframework.stereotype.Component;

import com.src.main.dto.SignupRequestDTO;
import com.src.main.model.UserInfo;
import com.src.main.service.UserDetailsServiceImpl;
import com.src.main.validators.AuthValidator;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class UserInfoTransformer {

	final UserDetailsServiceImpl userService;

	final AuthValidator authValidator;

	public UserInfo fromSignupRequestDTO(SignupRequestDTO signupRequest) {
		UserInfo userInfo = UserInfo.builder().username(signupRequest.getUsername()).password(signupRequest.getPassword()).build();
		if(authValidator.isMobileNumberProivded(signupRequest)) {
			userInfo.setMobile(signupRequest.getMobile());
		} else {
			userInfo.setEmail(signupRequest.getEmail());
		}
		return userInfo;
	}
}
