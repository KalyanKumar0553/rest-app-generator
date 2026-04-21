package com.src.main.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IdSpecDTO {
	private String field;
	private String type;
	private GenerationSpecDTO generation;

	public String getField() {
		return this.field;
	}

	public String getType() {
		return this.type;
	}

	public GenerationSpecDTO getGeneration() {
		return this.generation;
	}

	public void setField(final String field) {
		this.field = field;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public void setGeneration(final GenerationSpecDTO generation) {
		this.generation = generation;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof IdSpecDTO)) return false;
		final IdSpecDTO other = (IdSpecDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$field = this.getField();
		final Object other$field = other.getField();
		if (this$field == null ? other$field != null : !this$field.equals(other$field)) return false;
		final Object this$type = this.getType();
		final Object other$type = other.getType();
		if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
		final Object this$generation = this.getGeneration();
		final Object other$generation = other.getGeneration();
		if (this$generation == null ? other$generation != null : !this$generation.equals(other$generation)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof IdSpecDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $field = this.getField();
		result = result * PRIME + ($field == null ? 43 : $field.hashCode());
		final Object $type = this.getType();
		result = result * PRIME + ($type == null ? 43 : $type.hashCode());
		final Object $generation = this.getGeneration();
		result = result * PRIME + ($generation == null ? 43 : $generation.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "IdSpecDTO(field=" + this.getField() + ", type=" + this.getType() + ", generation=" + this.getGeneration() + ")";
	}

	public IdSpecDTO(final String field, final String type, final GenerationSpecDTO generation) {
		this.field = field;
		this.type = type;
		this.generation = generation;
	}

	public IdSpecDTO() {
	}
}
