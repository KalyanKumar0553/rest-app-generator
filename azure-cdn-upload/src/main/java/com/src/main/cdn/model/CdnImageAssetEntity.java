package com.src.main.cdn.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.src.main.cdn.config.AzureCdnDbTables;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = AzureCdnDbTables.IMAGE_UPLOAD_ASSETS)
public class CdnImageAssetEntity {

	@Id
	private UUID id;

	@Column(name = "draft_id", nullable = false, unique = true)
	private UUID draftId;

	@Column(name = "file_name", nullable = false, length = 255)
	private String fileName;

	@Column(name = "content_type", nullable = false, length = 150)
	private String contentType;

	@Column(name = "size_bytes", nullable = false)
	private long sizeBytes;

	@Column(name = "checksum_sha256", nullable = false, length = 128)
	private String checksumSha256;

	@Column(name = "storage_provider", nullable = false, length = 50)
	private String storageProvider;

	@Column(name = "container_name", nullable = false, length = 200)
	private String containerName;

	@Column(name = "blob_name", nullable = false, length = 500)
	private String blobName;

	@Column(name = "image_url", nullable = false, length = 1200)
	private String imageUrl;

	@Column(name = "created_by_user_id", nullable = false, length = 100)
	private String createdByUserId;

	@Column(name = "uploaded_by_user_id", nullable = false, length = 100)
	private String uploadedByUserId;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "uploaded_at", nullable = false)
	private OffsetDateTime uploadedAt;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getDraftId() {
		return draftId;
	}

	public void setDraftId(UUID draftId) {
		this.draftId = draftId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public long getSizeBytes() {
		return sizeBytes;
	}

	public void setSizeBytes(long sizeBytes) {
		this.sizeBytes = sizeBytes;
	}

	public String getChecksumSha256() {
		return checksumSha256;
	}

	public void setChecksumSha256(String checksumSha256) {
		this.checksumSha256 = checksumSha256;
	}

	public String getStorageProvider() {
		return storageProvider;
	}

	public void setStorageProvider(String storageProvider) {
		this.storageProvider = storageProvider;
	}

	public String getContainerName() {
		return containerName;
	}

	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	public String getBlobName() {
		return blobName;
	}

	public void setBlobName(String blobName) {
		this.blobName = blobName;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getCreatedByUserId() {
		return createdByUserId;
	}

	public void setCreatedByUserId(String createdByUserId) {
		this.createdByUserId = createdByUserId;
	}

	public String getUploadedByUserId() {
		return uploadedByUserId;
	}

	public void setUploadedByUserId(String uploadedByUserId) {
		this.uploadedByUserId = uploadedByUserId;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public OffsetDateTime getUploadedAt() {
		return uploadedAt;
	}

	public void setUploadedAt(OffsetDateTime uploadedAt) {
		this.uploadedAt = uploadedAt;
	}
}
