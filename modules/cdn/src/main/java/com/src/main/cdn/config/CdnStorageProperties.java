package com.src.main.cdn.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.media.cdn")
public class CdnStorageProperties {

	private boolean enabled;
	private String connectionString;
	private String containerName;
	private String cdnBaseUrl;
	private String blobPathPrefix;
	private String tenantCode;
	private int maxFiles;
	private long maxFileSizeBytes;
	private long maxVideoFileSizeBytes;
	private long schedulerFixedDelayMs;

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

	public int getMaxFiles() {
		return maxFiles;
	}

	public void setMaxFiles(int maxFiles) {
		this.maxFiles = maxFiles;
	}

	public long getMaxVideoFileSizeBytes() {
		return maxVideoFileSizeBytes;
	}

	public void setMaxVideoFileSizeBytes(long maxVideoFileSizeBytes) {
		this.maxVideoFileSizeBytes = maxVideoFileSizeBytes;
	}

	public long getSchedulerFixedDelayMs() {
		return schedulerFixedDelayMs;
	}

	public void setSchedulerFixedDelayMs(long schedulerFixedDelayMs) {
		this.schedulerFixedDelayMs = schedulerFixedDelayMs;
	}
}
