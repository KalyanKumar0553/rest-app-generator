package com.src.main.subscription.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.subscription")
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

	public boolean isEnabled() {
		return this.enabled;
	}

	public boolean isAdminApisEnabled() {
		return this.adminApisEnabled;
	}

	public boolean isSelfApisEnabled() {
		return this.selfApisEnabled;
	}

	public boolean isAspectEnabled() {
		return this.aspectEnabled;
	}

	public boolean isUsageTrackingEnabled() {
		return this.usageTrackingEnabled;
	}

	public boolean isAutoAssignDefaultPlan() {
		return this.autoAssignDefaultPlan;
	}

	public String getDefaultCurrency() {
		return this.defaultCurrency;
	}

	public boolean isFallbackToDefaultPlanOnExpiry() {
		return this.fallbackToDefaultPlanOnExpiry;
	}

	public String getTenantHeaderName() {
		return this.tenantHeaderName;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	public void setAdminApisEnabled(final boolean adminApisEnabled) {
		this.adminApisEnabled = adminApisEnabled;
	}

	public void setSelfApisEnabled(final boolean selfApisEnabled) {
		this.selfApisEnabled = selfApisEnabled;
	}

	public void setAspectEnabled(final boolean aspectEnabled) {
		this.aspectEnabled = aspectEnabled;
	}

	public void setUsageTrackingEnabled(final boolean usageTrackingEnabled) {
		this.usageTrackingEnabled = usageTrackingEnabled;
	}

	public void setAutoAssignDefaultPlan(final boolean autoAssignDefaultPlan) {
		this.autoAssignDefaultPlan = autoAssignDefaultPlan;
	}

	public void setDefaultCurrency(final String defaultCurrency) {
		this.defaultCurrency = defaultCurrency;
	}

	public void setFallbackToDefaultPlanOnExpiry(final boolean fallbackToDefaultPlanOnExpiry) {
		this.fallbackToDefaultPlanOnExpiry = fallbackToDefaultPlanOnExpiry;
	}

	public void setTenantHeaderName(final String tenantHeaderName) {
		this.tenantHeaderName = tenantHeaderName;
	}
}
