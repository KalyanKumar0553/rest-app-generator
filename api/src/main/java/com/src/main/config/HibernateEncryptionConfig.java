package com.src.main.config;

import java.util.Map;

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateEncryptionConfig {

	@Bean
	HibernatePropertiesCustomizer hibernateEncryptionPropertiesCustomizer(HibernateEncryptionInterceptor interceptor) {
		return (properties) -> properties.put("hibernate.session_factory.interceptor", interceptor);
	}
}
