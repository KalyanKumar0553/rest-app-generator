package com.src.main.subscription.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.src.main.subscription.security.HeaderSubscriptionTenantResolver;
import com.src.main.subscription.security.SubscriptionTenantResolver;

@Configuration
@EnableConfigurationProperties(SubscriptionModuleProperties.class)
@ConditionalOnProperty(prefix = "app.subscription", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SubscriptionModuleAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(SubscriptionTenantResolver.class)
	public SubscriptionTenantResolver subscriptionTenantResolver(SubscriptionModuleProperties properties) {
		return new HeaderSubscriptionTenantResolver(properties.getTenantHeaderName());
	}
}
