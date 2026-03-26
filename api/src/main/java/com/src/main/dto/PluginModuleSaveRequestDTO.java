package com.src.main.dto;

import java.util.List;

public class PluginModuleSaveRequestDTO {
	private String code;
	private String name;
	private String description;
	private String category;
	private Boolean enabled;
	private Boolean enableConfig;
	private List<String> generatorTargets;

	public PluginModuleSaveRequestDTO() {
	}

	public String getCode() {
		return this.code;
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

	public String getCategory() {
		return this.category;
	}

	public Boolean getEnabled() {
		return this.enabled;
	}

	public Boolean getEnableConfig() {
		return this.enableConfig;
	}

	public List<String> getGeneratorTargets() {
		return this.generatorTargets;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setCategory(final String category) {
		this.category = category;
	}

	public void setEnabled(final Boolean enabled) {
		this.enabled = enabled;
	}

	public void setEnableConfig(final Boolean enableConfig) {
		this.enableConfig = enableConfig;
	}

	public void setGeneratorTargets(final List<String> generatorTargets) {
		this.generatorTargets = generatorTargets;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof PluginModuleSaveRequestDTO)) return false;
		final PluginModuleSaveRequestDTO other = (PluginModuleSaveRequestDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$enabled = this.getEnabled();
		final Object other$enabled = other.getEnabled();
		if (this$enabled == null ? other$enabled != null : !this$enabled.equals(other$enabled)) return false;
		final Object this$enableConfig = this.getEnableConfig();
		final Object other$enableConfig = other.getEnableConfig();
		if (this$enableConfig == null ? other$enableConfig != null : !this$enableConfig.equals(other$enableConfig)) return false;
		final Object this$code = this.getCode();
		final Object other$code = other.getCode();
		if (this$code == null ? other$code != null : !this$code.equals(other$code)) return false;
		final Object this$name = this.getName();
		final Object other$name = other.getName();
		if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
		final Object this$description = this.getDescription();
		final Object other$description = other.getDescription();
		if (this$description == null ? other$description != null : !this$description.equals(other$description)) return false;
		final Object this$category = this.getCategory();
		final Object other$category = other.getCategory();
		if (this$category == null ? other$category != null : !this$category.equals(other$category)) return false;
		final Object this$generatorTargets = this.getGeneratorTargets();
		final Object other$generatorTargets = other.getGeneratorTargets();
		if (this$generatorTargets == null ? other$generatorTargets != null : !this$generatorTargets.equals(other$generatorTargets)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof PluginModuleSaveRequestDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $enabled = this.getEnabled();
		result = result * PRIME + ($enabled == null ? 43 : $enabled.hashCode());
		final Object $enableConfig = this.getEnableConfig();
		result = result * PRIME + ($enableConfig == null ? 43 : $enableConfig.hashCode());
		final Object $code = this.getCode();
		result = result * PRIME + ($code == null ? 43 : $code.hashCode());
		final Object $name = this.getName();
		result = result * PRIME + ($name == null ? 43 : $name.hashCode());
		final Object $description = this.getDescription();
		result = result * PRIME + ($description == null ? 43 : $description.hashCode());
		final Object $category = this.getCategory();
		result = result * PRIME + ($category == null ? 43 : $category.hashCode());
		final Object $generatorTargets = this.getGeneratorTargets();
		result = result * PRIME + ($generatorTargets == null ? 43 : $generatorTargets.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "PluginModuleSaveRequestDTO(code=" + this.getCode() + ", name=" + this.getName() + ", description=" + this.getDescription() + ", category=" + this.getCategory() + ", enabled=" + this.getEnabled() + ", enableConfig=" + this.getEnableConfig() + ", generatorTargets=" + this.getGeneratorTargets() + ")";
	}
}
