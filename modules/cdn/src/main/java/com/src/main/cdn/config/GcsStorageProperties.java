package com.src.main.cdn.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.media.gcs")
public class GcsStorageProperties {
	private boolean enabled;
	private String projectId;
	private String credentialsJson;
	private String bucketName;
	private String cdnBaseUrl;
	private String blobPathPrefix;
	private String tenantCode;
	public boolean isEnabled() { return enabled; }
	public void setEnabled(boolean enabled) { this.enabled = enabled; }
	public String getProjectId() { return projectId; }
	public void setProjectId(String projectId) { this.projectId = projectId; }
	public String getCredentialsJson() { return credentialsJson; }
	public void setCredentialsJson(String credentialsJson) { this.credentialsJson = credentialsJson; }
	public String getBucketName() { return bucketName; }
	public void setBucketName(String bucketName) { this.bucketName = bucketName; }
	public String getCdnBaseUrl() { return cdnBaseUrl; }
	public void setCdnBaseUrl(String cdnBaseUrl) { this.cdnBaseUrl = cdnBaseUrl; }
	public String getBlobPathPrefix() { return blobPathPrefix; }
	public void setBlobPathPrefix(String blobPathPrefix) { this.blobPathPrefix = blobPathPrefix; }
	public String getTenantCode() { return tenantCode; }
	public void setTenantCode(String tenantCode) { this.tenantCode = tenantCode; }
}
