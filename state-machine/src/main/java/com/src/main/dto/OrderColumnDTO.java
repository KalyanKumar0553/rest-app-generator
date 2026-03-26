package com.src.main.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderColumnDTO {
	private String name;

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof OrderColumnDTO)) return false;
		final OrderColumnDTO other = (OrderColumnDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$name = this.getName();
		final Object other$name = other.getName();
		if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof OrderColumnDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $name = this.getName();
		result = result * PRIME + ($name == null ? 43 : $name.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "OrderColumnDTO(name=" + this.getName() + ")";
	}

	public OrderColumnDTO(final String name) {
		this.name = name;
	}

	public OrderColumnDTO() {
	}
}
