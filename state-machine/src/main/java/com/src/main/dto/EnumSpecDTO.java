package com.src.main.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnumSpecDTO {
	private String name;
	private String storage;
	private List<String> constants;

	public String getName() {
		return this.name;
	}

	public String getStorage() {
		return this.storage;
	}

	public List<String> getConstants() {
		return this.constants;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setStorage(final String storage) {
		this.storage = storage;
	}

	public void setConstants(final List<String> constants) {
		this.constants = constants;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof EnumSpecDTO)) return false;
		final EnumSpecDTO other = (EnumSpecDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$name = this.getName();
		final Object other$name = other.getName();
		if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
		final Object this$storage = this.getStorage();
		final Object other$storage = other.getStorage();
		if (this$storage == null ? other$storage != null : !this$storage.equals(other$storage)) return false;
		final Object this$constants = this.getConstants();
		final Object other$constants = other.getConstants();
		if (this$constants == null ? other$constants != null : !this$constants.equals(other$constants)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof EnumSpecDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $name = this.getName();
		result = result * PRIME + ($name == null ? 43 : $name.hashCode());
		final Object $storage = this.getStorage();
		result = result * PRIME + ($storage == null ? 43 : $storage.hashCode());
		final Object $constants = this.getConstants();
		result = result * PRIME + ($constants == null ? 43 : $constants.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "EnumSpecDTO(name=" + this.getName() + ", storage=" + this.getStorage() + ", constants=" + this.getConstants() + ")";
	}

	public EnumSpecDTO(final String name, final String storage, final List<String> constants) {
		this.name = name;
		this.storage = storage;
		this.constants = constants;
	}

	public EnumSpecDTO() {
	}
}
