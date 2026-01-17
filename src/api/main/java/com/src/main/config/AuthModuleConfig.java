package com.src.main.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.src.main.auth.service.OtpSender;

@Configuration
public class AuthModuleConfig {

	@Bean
	@Primary
	public OtpSender otpSender(@Qualifier("otpSenderRouter") OtpSender otpSender) {
		return otpSender;
	}
}
