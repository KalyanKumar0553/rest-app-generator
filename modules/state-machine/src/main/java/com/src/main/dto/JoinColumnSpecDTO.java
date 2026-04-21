package com.src.main.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JoinColumnSpecDTO {
	private String name;
	private Boolean nullable;
	private String referencedColumnName;
	private Boolean index;
	private String onDelete; // NONE|CASCADE|SET_NULL (DDL hint)

	public String getName() {
		return this.name;
	}

	public Boolean getNullable() {
		return this.nullable;
	}

	public String getReferencedColumnName() {
		return this.referencedColumnName;
	}

	public Boolean getIndex() {
		return this.index;
	}

	public String getOnDelete() {
		return this.onDelete;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setNullable(final Boolean nullable) {
		this.nullable = nullable;
	}

	public void setReferencedColumnName(final String referencedColumnName) {
		this.referencedColumnName = referencedColumnName;
	}

	public void setIndex(final Boolean index) {
		this.index = index;
	}

	public void setOnDelete(final String onDelete) {
		this.onDelete = onDelete;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof JoinColumnSpecDTO)) return false;
		final JoinColumnSpecDTO other = (JoinColumnSpecDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$nullable = this.getNullable();
		final Object other$nullable = other.getNullable();
		if (this$nullable == null ? other$nullable != null : !this$nullable.equals(other$nullable)) return false;
		final Object this$index = this.getIndex();
		final Object other$index = other.getIndex();
		if (this$index == null ? other$index != null : !this$index.equals(other$index)) return false;
		final Object this$name = this.getName();
		final Object other$name = other.getName();
		if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
		final Object this$referencedColumnName = this.getReferencedColumnName();
		final Object other$referencedColumnName = other.getReferencedColumnName();
		if (this$referencedColumnName == null ? other$referencedColumnName != null : !this$referencedColumnName.equals(other$referencedColumnName)) return false;
		final Object this$onDelete = this.getOnDelete();
		final Object other$onDelete = other.getOnDelete();
		if (this$onDelete == null ? other$onDelete != null : !this$onDelete.equals(other$onDelete)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof JoinColumnSpecDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $nullable = this.getNullable();
		result = result * PRIME + ($nullable == null ? 43 : $nullable.hashCode());
		final Object $index = this.getIndex();
		result = result * PRIME + ($index == null ? 43 : $index.hashCode());
		final Object $name = this.getName();
		result = result * PRIME + ($name == null ? 43 : $name.hashCode());
		final Object $referencedColumnName = this.getReferencedColumnName();
		result = result * PRIME + ($referencedColumnName == null ? 43 : $referencedColumnName.hashCode());
		final Object $onDelete = this.getOnDelete();
		result = result * PRIME + ($onDelete == null ? 43 : $onDelete.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "JoinColumnSpecDTO(name=" + this.getName() + ", nullable=" + this.getNullable() + ", referencedColumnName=" + this.getReferencedColumnName() + ", index=" + this.getIndex() + ", onDelete=" + this.getOnDelete() + ")";
	}

	public JoinColumnSpecDTO(final String name, final Boolean nullable, final String referencedColumnName, final Boolean index, final String onDelete) {
		this.name = name;
		this.nullable = nullable;
		this.referencedColumnName = referencedColumnName;
		this.index = index;
		this.onDelete = onDelete;
	}

	public JoinColumnSpecDTO() {
	}
}
