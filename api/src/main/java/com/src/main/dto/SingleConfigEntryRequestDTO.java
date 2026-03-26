package com.src.main.dto;

public class SingleConfigEntryRequestDTO {
	private String category;
	private String propertyKey;
	private String valueKey;

	public String getCategory() {
		return this.category;
	}

	public String getPropertyKey() {
		return this.propertyKey;
	}

	public String getValueKey() {
		return this.valueKey;
	}

	public void setCategory(final String category) {
		this.category = category;
	}

	public void setPropertyKey(final String propertyKey) {
		this.propertyKey = propertyKey;
	}

	public void setValueKey(final String valueKey) {
		this.valueKey = valueKey;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof SingleConfigEntryRequestDTO)) return false;
		final SingleConfigEntryRequestDTO other = (SingleConfigEntryRequestDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$category = this.getCategory();
		final Object other$category = other.getCategory();
		if (this$category == null ? other$category != null : !this$category.equals(other$category)) return false;
		final Object this$propertyKey = this.getPropertyKey();
		final Object other$propertyKey = other.getPropertyKey();
		if (this$propertyKey == null ? other$propertyKey != null : !this$propertyKey.equals(other$propertyKey)) return false;
		final Object this$valueKey = this.getValueKey();
		final Object other$valueKey = other.getValueKey();
		if (this$valueKey == null ? other$valueKey != null : !this$valueKey.equals(other$valueKey)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof SingleConfigEntryRequestDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $category = this.getCategory();
		result = result * PRIME + ($category == null ? 43 : $category.hashCode());
		final Object $propertyKey = this.getPropertyKey();
		result = result * PRIME + ($propertyKey == null ? 43 : $propertyKey.hashCode());
		final Object $valueKey = this.getValueKey();
		result = result * PRIME + ($valueKey == null ? 43 : $valueKey.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "SingleConfigEntryRequestDTO(category=" + this.getCategory() + ", propertyKey=" + this.getPropertyKey() + ", valueKey=" + this.getValueKey() + ")";
	}

	public SingleConfigEntryRequestDTO(final String category, final String propertyKey, final String valueKey) {
		this.category = category;
		this.propertyKey = propertyKey;
		this.valueKey = valueKey;
	}

	public SingleConfigEntryRequestDTO() {
	}
}
