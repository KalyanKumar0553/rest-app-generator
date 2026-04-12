package com.src.main.cdn.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.src.main.cdn.config.AzureCdnDbTables;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = AzureCdnDbTables.IMAGE_UPLOAD_DRAFTS)
public class CdnImageUploadDraftEntity {

	@Id
	private UUID id;

	@Column(name = "file_name", nullable = false, length = 255)
	private String fileName;

	@Column(name = "content_type", nullable = false, length = 150)
	private String contentType;

	@Column(name = "size_bytes", nullable = false)
	private long sizeBytes;

	@Column(name = "checksum_sha256", nullable = false, length = 128)
	private String checksumSha256;

	@Lob
	@JdbcTypeCode(SqlTypes.LONGVARBINARY)
	@Column(name = "binary_data")
	private byte[] binaryData;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private CdnImageUploadStatus status;

	@Column(name = "storage_provider", nullable = false, length = 50)
	private String storageProvider;

	@Column(name = "created_by_user_id", nullable = false, length = 100)
	private String createdByUserId;

	@Column(name = "updated_by_user_id", nullable = false, length = 100)
	private String updatedByUserId;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@Column(name = "queued_at")
	private OffsetDateTime queuedAt;

	@Column(name = "processing_started_at")
	private OffsetDateTime processingStartedAt;

	@Column(name = "completed_at")
	private OffsetDateTime completedAt;

	@Column(name = "attempt_count", nullable = false)
	private int attemptCount;

	@Column(name = "last_error_message", length = 1000)
	private String lastErrorMessage;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
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

	public byte[] getBinaryData() {
		return binaryData;
	}

	public void setBinaryData(byte[] binaryData) {
		this.binaryData = binaryData;
	}

	public CdnImageUploadStatus getStatus() {
		return status;
	}

	public void setStatus(CdnImageUploadStatus status) {
		this.status = status;
	}

	public String getStorageProvider() {
		return storageProvider;
	}

	public void setStorageProvider(String storageProvider) {
		this.storageProvider = storageProvider;
	}

	public String getCreatedByUserId() {
		return createdByUserId;
	}

	public void setCreatedByUserId(String createdByUserId) {
		this.createdByUserId = createdByUserId;
	}

	public String getUpdatedByUserId() {
		return updatedByUserId;
	}

	public void setUpdatedByUserId(String updatedByUserId) {
		this.updatedByUserId = updatedByUserId;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(OffsetDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public OffsetDateTime getQueuedAt() {
		return queuedAt;
	}

	public void setQueuedAt(OffsetDateTime queuedAt) {
		this.queuedAt = queuedAt;
	}

	public OffsetDateTime getProcessingStartedAt() {
		return processingStartedAt;
	}

	public void setProcessingStartedAt(OffsetDateTime processingStartedAt) {
		this.processingStartedAt = processingStartedAt;
	}

	public OffsetDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(OffsetDateTime completedAt) {
		this.completedAt = completedAt;
	}

	public int getAttemptCount() {
		return attemptCount;
	}

	public void setAttemptCount(int attemptCount) {
		this.attemptCount = attemptCount;
	}

	public String getLastErrorMessage() {
		return lastErrorMessage;
	}

	public void setLastErrorMessage(String lastErrorMessage) {
		this.lastErrorMessage = lastErrorMessage;
	}
}
