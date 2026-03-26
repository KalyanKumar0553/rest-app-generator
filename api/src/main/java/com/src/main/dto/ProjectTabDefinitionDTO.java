package com.src.main.dto;

public class ProjectTabDefinitionDTO {
	private String key;
	private String label;
	private String icon;
	private String componentKey;
	private int order;

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

	public int getOrder() {
		return this.order;
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

	public void setOrder(final int order) {
		this.order = order;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectTabDefinitionDTO)) return false;
		final ProjectTabDefinitionDTO other = (ProjectTabDefinitionDTO) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.getOrder() != other.getOrder()) return false;
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
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectTabDefinitionDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.getOrder();
		final Object $key = this.getKey();
		result = result * PRIME + ($key == null ? 43 : $key.hashCode());
		final Object $label = this.getLabel();
		result = result * PRIME + ($label == null ? 43 : $label.hashCode());
		final Object $icon = this.getIcon();
		result = result * PRIME + ($icon == null ? 43 : $icon.hashCode());
		final Object $componentKey = this.getComponentKey();
		result = result * PRIME + ($componentKey == null ? 43 : $componentKey.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectTabDefinitionDTO(key=" + this.getKey() + ", label=" + this.getLabel() + ", icon=" + this.getIcon() + ", componentKey=" + this.getComponentKey() + ", order=" + this.getOrder() + ")";
	}

	public ProjectTabDefinitionDTO() {
	}

	public ProjectTabDefinitionDTO(final String key, final String label, final String icon, final String componentKey, final int order) {
		this.key = key;
		this.label = label;
		this.icon = icon;
		this.componentKey = componentKey;
		this.order = order;
	}
}
