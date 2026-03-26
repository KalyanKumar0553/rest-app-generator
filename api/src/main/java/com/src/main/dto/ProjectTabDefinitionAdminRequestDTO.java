package com.src.main.dto;

public class ProjectTabDefinitionAdminRequestDTO {
	private String key;
	private String label;
	private String icon;
	private String componentKey;
	private Integer order;
	private String generatorLanguage;
	private Boolean enabled;

	public ProjectTabDefinitionAdminRequestDTO() {
	}

	public String getKey() {
		return this.key;
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

	public Integer getOrder() {
		return this.order;
	}

	public String getGeneratorLanguage() {
		return this.generatorLanguage;
	}

	public Boolean getEnabled() {
		return this.enabled;
	}

	public void setKey(final String key) {
		this.key = key;
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

	public void setOrder(final Integer order) {
		this.order = order;
	}

	public void setGeneratorLanguage(final String generatorLanguage) {
		this.generatorLanguage = generatorLanguage;
	}

	public void setEnabled(final Boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectTabDefinitionAdminRequestDTO)) return false;
		final ProjectTabDefinitionAdminRequestDTO other = (ProjectTabDefinitionAdminRequestDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$order = this.getOrder();
		final Object other$order = other.getOrder();
		if (this$order == null ? other$order != null : !this$order.equals(other$order)) return false;
		final Object this$enabled = this.getEnabled();
		final Object other$enabled = other.getEnabled();
		if (this$enabled == null ? other$enabled != null : !this$enabled.equals(other$enabled)) return false;
		final Object this$key = this.getKey();
		final Object other$key = other.getKey();
		if (this$key == null ? other$key != null : !this$key.equals(other$key)) return false;
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
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectTabDefinitionAdminRequestDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $order = this.getOrder();
		result = result * PRIME + ($order == null ? 43 : $order.hashCode());
		final Object $enabled = this.getEnabled();
		result = result * PRIME + ($enabled == null ? 43 : $enabled.hashCode());
		final Object $key = this.getKey();
		result = result * PRIME + ($key == null ? 43 : $key.hashCode());
		final Object $label = this.getLabel();
		result = result * PRIME + ($label == null ? 43 : $label.hashCode());
		final Object $icon = this.getIcon();
		result = result * PRIME + ($icon == null ? 43 : $icon.hashCode());
		final Object $componentKey = this.getComponentKey();
		result = result * PRIME + ($componentKey == null ? 43 : $componentKey.hashCode());
		final Object $generatorLanguage = this.getGeneratorLanguage();
		result = result * PRIME + ($generatorLanguage == null ? 43 : $generatorLanguage.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectTabDefinitionAdminRequestDTO(key=" + this.getKey() + ", label=" + this.getLabel() + ", icon=" + this.getIcon() + ", componentKey=" + this.getComponentKey() + ", order=" + this.getOrder() + ", generatorLanguage=" + this.getGeneratorLanguage() + ", enabled=" + this.getEnabled() + ")";
	}
}
