package com.src.main.cdn.model;

import java.time.OffsetDateTime;

import com.src.main.cdn.config.CdnDbTables;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = CdnDbTables.IMAGE_UPLOAD_SETTINGS)
public class CdnImageUploadSettingsEntity {

	public static final String SINGLETON_ID = "default";

	@Id
	@Column(length = 50)
	private String id;

	@Column(name = "batch_processing_enabled", nullable = false)
	private boolean batchProcessingEnabled;

	@Column(name = "updated_by_user_id", nullable = false, length = 100)
	private String updatedByUserId;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isBatchProcessingEnabled() {
		return batchProcessingEnabled;
	}

	public void setBatchProcessingEnabled(boolean batchProcessingEnabled) {
		this.batchProcessingEnabled = batchProcessingEnabled;
	}

	public String getUpdatedByUserId() {
		return updatedByUserId;
	}

	public void setUpdatedByUserId(String updatedByUserId) {
		this.updatedByUserId = updatedByUserId;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(OffsetDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
