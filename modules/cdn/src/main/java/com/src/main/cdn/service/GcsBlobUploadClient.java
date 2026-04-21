package com.src.main.cdn.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.src.main.cdn.config.GcsStorageProperties;
import com.src.main.storage.MediaStorageUploadClient;

@Component
@ConditionalOnProperty(name = "app.media.storage-provider", havingValue = "GCS")
public class GcsBlobUploadClient implements MediaStorageUploadClient {
	private final GcsStorageProperties properties;
	public GcsBlobUploadClient(GcsStorageProperties properties) { this.properties = properties; }
	@Override public String providerName() { return "GCS"; }
	@Override
	public UploadResult upload(byte[] bytes, String fileName, String contentType) {
		if (!properties.isEnabled()) throw new IllegalStateException("GCS upload is disabled.");
		Storage storage = StringUtils.hasText(properties.getProjectId())
				? StorageOptions.newBuilder().setProjectId(properties.getProjectId().trim()).build().getService()
				: StorageOptions.getDefaultInstance().getService();
		String bucket = properties.getBucketName().trim();
		String key = buildBlobName(fileName);
		storage.create(BlobInfo.newBuilder(BlobId.of(bucket, key)).setContentType(contentType).build(), bytes);
		String imageUrl = StringUtils.hasText(properties.getCdnBaseUrl())
				? properties.getCdnBaseUrl().trim().replaceAll("/+$", "") + "/" + key
				: "https://storage.googleapis.com/" + bucket + "/" + key;
		return new UploadResult(bucket, key, imageUrl);
	}

	@Override
	public void delete(String containerName, String blobName) {
		if (!properties.isEnabled() || !StringUtils.hasText(containerName) || !StringUtils.hasText(blobName)) {
			return;
		}
		Storage storage = StringUtils.hasText(properties.getProjectId())
				? StorageOptions.newBuilder().setProjectId(properties.getProjectId().trim()).build().getService()
				: StorageOptions.getDefaultInstance().getService();
		storage.delete(BlobId.of(containerName.trim(), blobName.trim()));
	}

	private String buildBlobName(String fileName) {
		String prefix = normalizeSegment(properties.getBlobPathPrefix(), "uploads");
		String tenantSegment = normalizeSegment(properties.getTenantCode(), "default");
		String datePath = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.ROOT).format(OffsetDateTime.now(ZoneOffset.UTC));
		String safeFileName = fileName == null ? "image.bin" : fileName.replaceAll("[^a-zA-Z0-9._-]+", "-");
		return prefix + "/" + tenantSegment + "/" + datePath + "/" + UUID.randomUUID() + "-" + safeFileName;
	}
	private String normalizeSegment(String value, String defaultValue) {
		if (!StringUtils.hasText(value)) return defaultValue;
		String normalized = value.trim().replace('\\', '/').replaceAll("^/+", "").replaceAll("/+$", "")
				.replaceAll("[^a-zA-Z0-9/_-]+", "-").replaceAll("/{2,}", "/");
		return StringUtils.hasText(normalized) ? normalized : defaultValue;
	}
}
