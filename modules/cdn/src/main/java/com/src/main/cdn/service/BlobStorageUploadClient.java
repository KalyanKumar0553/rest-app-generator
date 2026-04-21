package com.src.main.cdn.service;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.src.main.cdn.config.CdnStorageProperties;
import com.src.main.storage.MediaStorageUploadClient;

@Component
@ConditionalOnProperty(name = "app.media.storage-provider", havingValue = "AZURE_CDN", matchIfMissing = true)
public class BlobStorageUploadClient implements MediaStorageUploadClient {

	private final CdnStorageProperties properties;

	public BlobStorageUploadClient(CdnStorageProperties properties) {
		this.properties = properties;
	}

	public MediaStorageUploadClient.UploadResult upload(byte[] bytes, String fileName, String contentType) {
		if (!properties.isEnabled()) {
			throw new IllegalStateException("CDN upload is disabled.");
		}
		if (!StringUtils.hasText(properties.getConnectionString())) {
			throw new IllegalStateException("Azure CDN connection string is not configured.");
		}
		BlobServiceClient serviceClient = new BlobServiceClientBuilder()
				.connectionString(properties.getConnectionString().trim())
				.buildClient();
		BlobContainerClient containerClient = serviceClient.getBlobContainerClient(properties.getContainerName().trim());
		if (!containerClient.exists()) {
			containerClient.create();
		}
		String blobName = buildBlobName(fileName);
		var blobClient = containerClient.getBlobClient(blobName);
		blobClient.upload(new ByteArrayInputStream(bytes), bytes.length, true);
		blobClient.setHttpHeaders(new com.azure.storage.blob.models.BlobHttpHeaders().setContentType(contentType));
		String imageUrl = buildPublicUrl(blobName, blobClient.getBlobUrl());
		return new UploadResult(properties.getContainerName().trim(), blobName, imageUrl);
	}

	@Override
	public void delete(String containerName, String blobName) {
		if (!properties.isEnabled()) {
			return;
		}
		if (!StringUtils.hasText(properties.getConnectionString())
				|| !StringUtils.hasText(containerName)
				|| !StringUtils.hasText(blobName)) {
			return;
		}
		BlobServiceClient serviceClient = new BlobServiceClientBuilder()
				.connectionString(properties.getConnectionString().trim())
				.buildClient();
		BlobContainerClient containerClient = serviceClient.getBlobContainerClient(containerName.trim());
		if (containerClient.exists()) {
			containerClient.getBlobClient(blobName.trim()).deleteIfExists();
		}
	}

	@Override
	public String providerName() {
		return "AZURE_CDN";
	}

	private String buildBlobName(String fileName) {
		String prefix = normalizeSegment(properties.getBlobPathPrefix(), "uploads");
		String tenantSegment = normalizeSegment(properties.getTenantCode(), "default");
		String datePath = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.ROOT)
				.format(OffsetDateTime.now(ZoneOffset.UTC));
		String safeFileName = fileName == null ? "image.bin" : fileName.replaceAll("[^a-zA-Z0-9._-]+", "-");
		return prefix + "/" + tenantSegment + "/" + datePath + "/" + UUID.randomUUID() + "-" + safeFileName;
	}

	private String buildPublicUrl(String blobName, String blobUrl) {
		if (StringUtils.hasText(properties.getCdnBaseUrl())) {
			return properties.getCdnBaseUrl().trim().replaceAll("/+$", "") + "/" + blobName;
		}
		return blobUrl;
	}

	private String normalizeSegment(String value, String defaultValue) {
		if (!StringUtils.hasText(value)) {
			return defaultValue;
		}
		String normalized = value.trim()
				.replace('\\', '/')
				.replaceAll("^/+", "")
				.replaceAll("/+$", "")
				.replaceAll("[^a-zA-Z0-9/_-]+", "-")
				.replaceAll("/{2,}", "/");
		return StringUtils.hasText(normalized) ? normalized : defaultValue;
	}

}
