package com.src.main.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.src.main.config.AppDbTables;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = AppDbTables.PROJECT_TAB_DEFINITIONS)
@Data
public class ProjectTabDefinitionEntity {

	@Id
	private UUID id;

	@Column(name = "tab_key", nullable = false, length = 100)
	private String tabKey;

	@Column(nullable = false, length = 150)
	private String label;

	@Column(nullable = false, length = 100)
	private String icon;

	@Column(name = "component_key", nullable = false, length = 100)
	private String componentKey;

	@Column(name = "display_order", nullable = false)
	private int displayOrder;

	@Column(name = "generator_language", nullable = false, length = 50)
	private String generatorLanguage;

	@Column(nullable = false)
	private boolean enabled;

	@Column(name = "created_by_user_id", nullable = false, length = 100)
	private String createdByUserId;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;
}
