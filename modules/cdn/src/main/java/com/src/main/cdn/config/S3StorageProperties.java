package com.src.main.cdn.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.media.s3")
public class S3StorageProperties {
	private boolean enabled;
	private String accessKeyId;
	private String secretAccessKey;
	private String region;
	private String bucketName;
	private String endpoint;
	private String cdnBaseUrl;
	private String blobPathPrefix;
	private String tenantCode;
	public boolean isEnabled() { return enabled; }
	public void setEnabled(boolean enabled) { this.enabled = enabled; }
	public String getAccessKeyId() { return accessKeyId; }
	public void setAccessKeyId(String accessKeyId) { this.accessKeyId = accessKeyId; }
	public String getSecretAccessKey() { return secretAccessKey; }
	public void setSecretAccessKey(String secretAccessKey) { this.secretAccessKey = secretAccessKey; }
	public String getRegion() { return region; }
	public void setRegion(String region) { this.region = region; }
	public String getBucketName() { return bucketName; }
	public void setBucketName(String bucketName) { this.bucketName = bucketName; }
	public String getEndpoint() { return endpoint; }
	public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
	public String getCdnBaseUrl() { return cdnBaseUrl; }
	public void setCdnBaseUrl(String cdnBaseUrl) { this.cdnBaseUrl = cdnBaseUrl; }
	public String getBlobPathPrefix() { return blobPathPrefix; }
	public void setBlobPathPrefix(String blobPathPrefix) { this.blobPathPrefix = blobPathPrefix; }
	public String getTenantCode() { return tenantCode; }
	public void setTenantCode(String tenantCode) { this.tenantCode = tenantCode; }
}
