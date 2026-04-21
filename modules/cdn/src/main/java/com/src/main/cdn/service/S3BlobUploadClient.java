package com.src.main.cdn.service;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.src.main.cdn.config.S3StorageProperties;
import com.src.main.storage.MediaStorageUploadClient;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@ConditionalOnProperty(name = "app.media.storage-provider", havingValue = "AZURE_S3")
public class S3BlobUploadClient implements MediaStorageUploadClient {
	private final S3StorageProperties properties;
	public S3BlobUploadClient(S3StorageProperties properties) { this.properties = properties; }
	@Override public String providerName() { return "AZURE_S3"; }
	@Override
	public UploadResult upload(byte[] bytes, String fileName, String contentType) {
		if (!properties.isEnabled()) throw new IllegalStateException("S3 upload is disabled.");
		Region region = StringUtils.hasText(properties.getRegion()) ? Region.of(properties.getRegion().trim()) : Region.US_EAST_1;
		try (S3Client s3 = buildClient(region)) {
			String bucket = properties.getBucketName().trim();
			String key = buildBlobName(fileName);
			s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key).contentType(contentType).build(),
					RequestBody.fromInputStream(new ByteArrayInputStream(bytes), bytes.length));
			String imageUrl = StringUtils.hasText(properties.getCdnBaseUrl())
					? properties.getCdnBaseUrl().trim().replaceAll("/+$", "") + "/" + key
					: "https://s3.amazonaws.com/" + bucket + "/" + key;
			return new UploadResult(bucket, key, imageUrl);
		}
	}

	@Override
	public void delete(String containerName, String blobName) {
		if (!properties.isEnabled() || !StringUtils.hasText(containerName) || !StringUtils.hasText(blobName)) {
			return;
		}
		Region region = StringUtils.hasText(properties.getRegion()) ? Region.of(properties.getRegion().trim()) : Region.US_EAST_1;
		try (S3Client s3 = buildClient(region)) {
			s3.deleteObject(builder -> builder.bucket(containerName.trim()).key(blobName.trim()));
		}
	}

	private S3Client buildClient(Region region) {
		var builder = S3Client.builder().region(region)
				.credentialsProvider(() -> AwsBasicCredentials.create(properties.getAccessKeyId().trim(), properties.getSecretAccessKey().trim()));
		if (StringUtils.hasText(properties.getEndpoint())) builder.endpointOverride(java.net.URI.create(properties.getEndpoint().trim()));
		return builder.build();
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
