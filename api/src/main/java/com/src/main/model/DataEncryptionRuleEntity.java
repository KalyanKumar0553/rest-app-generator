package com.src.main.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.src.main.config.AppDbTables;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = AppDbTables.DATA_ENCRYPTION_RULES)
@Data
public class DataEncryptionRuleEntity {

	@Id
	private UUID id;

	@Column(name = "table_name", nullable = false, length = 150)
	private String tableName;

	@Column(name = "column_name", length = 150)
	private String columnName;

	@Column(name = "hash_shadow_column", length = 150)
	private String hashShadowColumn;

	@Column(name = "enabled", nullable = false)
	private boolean enabled;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@PrePersist
	public void prePersist() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		OffsetDateTime now = OffsetDateTime.now();
		if (createdAt == null) {
			createdAt = now;
		}
		if (updatedAt == null) {
			updatedAt = now;
		}
	}

	@PreUpdate
	public void preUpdate() {
		updatedAt = OffsetDateTime.now();
	}
}
