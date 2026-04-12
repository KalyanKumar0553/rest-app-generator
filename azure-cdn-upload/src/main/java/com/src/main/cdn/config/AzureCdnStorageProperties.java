package com.src.main.cdn.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.azure.cdn")
public class AzureCdnStorageProperties {

	private boolean enabled;
	private String connectionString;
	private String containerName = "images";
	private String cdnBaseUrl;
	private String blobPathPrefix = "uploads";
	private String tenantCode = "default";
	private long maxFileSizeBytes = 10 * 1024 * 1024;
	private long schedulerFixedDelayMs = 10_000L;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getConnectionString() {
		return connectionString;
	}

	public void setConnectionString(String connectionString) {
		this.connectionString = connectionString;
	}

	public String getContainerName() {
		return containerName;
	}

	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	public String getCdnBaseUrl() {
		return cdnBaseUrl;
	}

	public void setCdnBaseUrl(String cdnBaseUrl) {
		this.cdnBaseUrl = cdnBaseUrl;
	}

	public String getBlobPathPrefix() {
		return blobPathPrefix;
	}

	public void setBlobPathPrefix(String blobPathPrefix) {
		this.blobPathPrefix = blobPathPrefix;
	}

	public String getTenantCode() {
		return tenantCode;
	}

	public void setTenantCode(String tenantCode) {
		this.tenantCode = tenantCode;
	}

	public long getMaxFileSizeBytes() {
		return maxFileSizeBytes;
	}

	public void setMaxFileSizeBytes(long maxFileSizeBytes) {
		this.maxFileSizeBytes = maxFileSizeBytes;
	}

	public long getSchedulerFixedDelayMs() {
		return schedulerFixedDelayMs;
	}

	public void setSchedulerFixedDelayMs(long schedulerFixedDelayMs) {
		this.schedulerFixedDelayMs = schedulerFixedDelayMs;
	}
}
