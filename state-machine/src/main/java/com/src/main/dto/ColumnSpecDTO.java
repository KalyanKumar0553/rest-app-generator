package com.src.main.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ColumnSpecDTO {
	private String name;
	private Boolean unique;
	private Boolean nullable;
	private Integer length;
	private String columnDefinition;

	public String getName() {
		return this.name;
	}

	public Boolean getUnique() {
		return this.unique;
	}

	public Boolean getNullable() {
		return this.nullable;
	}

	public Integer getLength() {
		return this.length;
	}

	public String getColumnDefinition() {
		return this.columnDefinition;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setUnique(final Boolean unique) {
		this.unique = unique;
	}

	public void setNullable(final Boolean nullable) {
		this.nullable = nullable;
	}

	public void setLength(final Integer length) {
		this.length = length;
	}

	public void setColumnDefinition(final String columnDefinition) {
		this.columnDefinition = columnDefinition;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ColumnSpecDTO)) return false;
		final ColumnSpecDTO other = (ColumnSpecDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$unique = this.getUnique();
		final Object other$unique = other.getUnique();
		if (this$unique == null ? other$unique != null : !this$unique.equals(other$unique)) return false;
		final Object this$nullable = this.getNullable();
		final Object other$nullable = other.getNullable();
		if (this$nullable == null ? other$nullable != null : !this$nullable.equals(other$nullable)) return false;
		final Object this$length = this.getLength();
		final Object other$length = other.getLength();
		if (this$length == null ? other$length != null : !this$length.equals(other$length)) return false;
		final Object this$name = this.getName();
		final Object other$name = other.getName();
		if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
		final Object this$columnDefinition = this.getColumnDefinition();
		final Object other$columnDefinition = other.getColumnDefinition();
		if (this$columnDefinition == null ? other$columnDefinition != null : !this$columnDefinition.equals(other$columnDefinition)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ColumnSpecDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $unique = this.getUnique();
		result = result * PRIME + ($unique == null ? 43 : $unique.hashCode());
		final Object $nullable = this.getNullable();
		result = result * PRIME + ($nullable == null ? 43 : $nullable.hashCode());
		final Object $length = this.getLength();
		result = result * PRIME + ($length == null ? 43 : $length.hashCode());
		final Object $name = this.getName();
		result = result * PRIME + ($name == null ? 43 : $name.hashCode());
		final Object $columnDefinition = this.getColumnDefinition();
		result = result * PRIME + ($columnDefinition == null ? 43 : $columnDefinition.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ColumnSpecDTO(name=" + this.getName() + ", unique=" + this.getUnique() + ", nullable=" + this.getNullable() + ", length=" + this.getLength() + ", columnDefinition=" + this.getColumnDefinition() + ")";
	}

	public ColumnSpecDTO(final String name, final Boolean unique, final Boolean nullable, final Integer length, final String columnDefinition) {
		this.name = name;
		this.unique = unique;
		this.nullable = nullable;
		this.length = length;
		this.columnDefinition = columnDefinition;
	}

	public ColumnSpecDTO() {
	}
}
