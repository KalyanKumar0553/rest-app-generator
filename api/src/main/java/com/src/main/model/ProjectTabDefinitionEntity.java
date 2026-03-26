package com.src.main.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.src.main.config.AppDbTables;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = AppDbTables.PROJECT_TAB_DEFINITIONS)
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

	public ProjectTabDefinitionEntity() {
	}

	public UUID getId() {
		return this.id;
	}

	public String getTabKey() {
		return this.tabKey;
	}

	public String getLabel() {
		return this.label;
	}

	public String getIcon() {
		return this.icon;
	}

	public String getComponentKey() {
		return this.componentKey;
	}

	public int getDisplayOrder() {
		return this.displayOrder;
	}

	public String getGeneratorLanguage() {
		return this.generatorLanguage;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public String getCreatedByUserId() {
		return this.createdByUserId;
	}

	public OffsetDateTime getCreatedAt() {
		return this.createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return this.updatedAt;
	}

	public void setId(final UUID id) {
		this.id = id;
	}

	public void setTabKey(final String tabKey) {
		this.tabKey = tabKey;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	public void setIcon(final String icon) {
		this.icon = icon;
	}

	public void setComponentKey(final String componentKey) {
		this.componentKey = componentKey;
	}

	public void setDisplayOrder(final int displayOrder) {
		this.displayOrder = displayOrder;
	}

	public void setGeneratorLanguage(final String generatorLanguage) {
		this.generatorLanguage = generatorLanguage;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	public void setCreatedByUserId(final String createdByUserId) {
		this.createdByUserId = createdByUserId;
	}

	public void setCreatedAt(final OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(final OffsetDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectTabDefinitionEntity)) return false;
		final ProjectTabDefinitionEntity other = (ProjectTabDefinitionEntity) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.getDisplayOrder() != other.getDisplayOrder()) return false;
		if (this.isEnabled() != other.isEnabled()) return false;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
		final Object this$tabKey = this.getTabKey();
		final Object other$tabKey = other.getTabKey();
		if (this$tabKey == null ? other$tabKey != null : !this$tabKey.equals(other$tabKey)) return false;
		final Object this$label = this.getLabel();
		final Object other$label = other.getLabel();
		if (this$label == null ? other$label != null : !this$label.equals(other$label)) return false;
		final Object this$icon = this.getIcon();
		final Object other$icon = other.getIcon();
		if (this$icon == null ? other$icon != null : !this$icon.equals(other$icon)) return false;
		final Object this$componentKey = this.getComponentKey();
		final Object other$componentKey = other.getComponentKey();
		if (this$componentKey == null ? other$componentKey != null : !this$componentKey.equals(other$componentKey)) return false;
		final Object this$generatorLanguage = this.getGeneratorLanguage();
		final Object other$generatorLanguage = other.getGeneratorLanguage();
		if (this$generatorLanguage == null ? other$generatorLanguage != null : !this$generatorLanguage.equals(other$generatorLanguage)) return false;
		final Object this$createdByUserId = this.getCreatedByUserId();
		final Object other$createdByUserId = other.getCreatedByUserId();
		if (this$createdByUserId == null ? other$createdByUserId != null : !this$createdByUserId.equals(other$createdByUserId)) return false;
		final Object this$createdAt = this.getCreatedAt();
		final Object other$createdAt = other.getCreatedAt();
		if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
		final Object this$updatedAt = this.getUpdatedAt();
		final Object other$updatedAt = other.getUpdatedAt();
		if (this$updatedAt == null ? other$updatedAt != null : !this$updatedAt.equals(other$updatedAt)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectTabDefinitionEntity;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.getDisplayOrder();
		result = result * PRIME + (this.isEnabled() ? 79 : 97);
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $tabKey = this.getTabKey();
		result = result * PRIME + ($tabKey == null ? 43 : $tabKey.hashCode());
		final Object $label = this.getLabel();
		result = result * PRIME + ($label == null ? 43 : $label.hashCode());
		final Object $icon = this.getIcon();
		result = result * PRIME + ($icon == null ? 43 : $icon.hashCode());
		final Object $componentKey = this.getComponentKey();
		result = result * PRIME + ($componentKey == null ? 43 : $componentKey.hashCode());
		final Object $generatorLanguage = this.getGeneratorLanguage();
		result = result * PRIME + ($generatorLanguage == null ? 43 : $generatorLanguage.hashCode());
		final Object $createdByUserId = this.getCreatedByUserId();
		result = result * PRIME + ($createdByUserId == null ? 43 : $createdByUserId.hashCode());
		final Object $createdAt = this.getCreatedAt();
		result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
		final Object $updatedAt = this.getUpdatedAt();
		result = result * PRIME + ($updatedAt == null ? 43 : $updatedAt.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectTabDefinitionEntity(id=" + this.getId() + ", tabKey=" + this.getTabKey() + ", label=" + this.getLabel() + ", icon=" + this.getIcon() + ", componentKey=" + this.getComponentKey() + ", displayOrder=" + this.getDisplayOrder() + ", generatorLanguage=" + this.getGeneratorLanguage() + ", enabled=" + this.isEnabled() + ", createdByUserId=" + this.getCreatedByUserId() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ")";
	}
}
