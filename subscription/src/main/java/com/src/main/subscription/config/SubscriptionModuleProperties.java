package com.src.main.subscription.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "app.subscription")
@Getter
@Setter
public class SubscriptionModuleProperties {
	private boolean enabled = true;
	private boolean adminApisEnabled = true;
	private boolean selfApisEnabled = true;
	private boolean aspectEnabled = true;
	private boolean usageTrackingEnabled = true;
	private boolean autoAssignDefaultPlan = true;
	private String defaultCurrency = "INR";
	private boolean fallbackToDefaultPlanOnExpiry = true;
	private String tenantHeaderName = "X-Tenant-Id";
}
