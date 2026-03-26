package com.src.main.dto;

import java.util.List;
import java.util.Map;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ArtifactAppRequestDTO {
	@NotBlank(message = "Code is required")
	@Size(max = 100, message = "Code can contain up to 100 characters")
	private String code;
	@NotBlank(message = "Name is required")
	@Size(max = 150, message = "Name can contain up to 150 characters")
	private String name;
	@Size(max = 5000, message = "Description can contain up to 5000 characters")
	private String description;
	@NotBlank(message = "Status is required")
	@Size(max = 32, message = "Status can contain up to 32 characters")
	private String status;
	@NotBlank(message = "Generator language is required")
	@Size(max = 32, message = "Generator language can contain up to 32 characters")
	private String generatorLanguage;
	@NotBlank(message = "Build tool is required")
	@Size(max = 32, message = "Build tool can contain up to 32 characters")
	private String buildTool;
	@NotNull(message = "Enabled packs are required")
	private List<String> enabledPacks;
	@NotNull(message = "Config is required")
	private Map<String, Object> config;

	public ArtifactAppRequestDTO() {
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

	public String getStatus() {
		return this.status;
	}

	public String getGeneratorLanguage() {
		return this.generatorLanguage;
	}

	public String getBuildTool() {
		return this.buildTool;
	}

	public List<String> getEnabledPacks() {
		return this.enabledPacks;
	}

	public Map<String, Object> getConfig() {
		return this.config;
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

	public void setStatus(final String status) {
		this.status = status;
	}

	public void setGeneratorLanguage(final String generatorLanguage) {
		this.generatorLanguage = generatorLanguage;
	}

	public void setBuildTool(final String buildTool) {
		this.buildTool = buildTool;
	}

	public void setEnabledPacks(final List<String> enabledPacks) {
		this.enabledPacks = enabledPacks;
	}

	public void setConfig(final Map<String, Object> config) {
		this.config = config;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ArtifactAppRequestDTO)) return false;
		final ArtifactAppRequestDTO other = (ArtifactAppRequestDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$code = this.getCode();
		final Object other$code = other.getCode();
		if (this$code == null ? other$code != null : !this$code.equals(other$code)) return false;
		final Object this$name = this.getName();
		final Object other$name = other.getName();
		if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
		final Object this$description = this.getDescription();
		final Object other$description = other.getDescription();
		if (this$description == null ? other$description != null : !this$description.equals(other$description)) return false;
		final Object this$status = this.getStatus();
		final Object other$status = other.getStatus();
		if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
		final Object this$generatorLanguage = this.getGeneratorLanguage();
		final Object other$generatorLanguage = other.getGeneratorLanguage();
		if (this$generatorLanguage == null ? other$generatorLanguage != null : !this$generatorLanguage.equals(other$generatorLanguage)) return false;
		final Object this$buildTool = this.getBuildTool();
		final Object other$buildTool = other.getBuildTool();
		if (this$buildTool == null ? other$buildTool != null : !this$buildTool.equals(other$buildTool)) return false;
		final Object this$enabledPacks = this.getEnabledPacks();
		final Object other$enabledPacks = other.getEnabledPacks();
		if (this$enabledPacks == null ? other$enabledPacks != null : !this$enabledPacks.equals(other$enabledPacks)) return false;
		final Object this$config = this.getConfig();
		final Object other$config = other.getConfig();
		if (this$config == null ? other$config != null : !this$config.equals(other$config)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ArtifactAppRequestDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $code = this.getCode();
		result = result * PRIME + ($code == null ? 43 : $code.hashCode());
		final Object $name = this.getName();
		result = result * PRIME + ($name == null ? 43 : $name.hashCode());
		final Object $description = this.getDescription();
		result = result * PRIME + ($description == null ? 43 : $description.hashCode());
		final Object $status = this.getStatus();
		result = result * PRIME + ($status == null ? 43 : $status.hashCode());
		final Object $generatorLanguage = this.getGeneratorLanguage();
		result = result * PRIME + ($generatorLanguage == null ? 43 : $generatorLanguage.hashCode());
		final Object $buildTool = this.getBuildTool();
		result = result * PRIME + ($buildTool == null ? 43 : $buildTool.hashCode());
		final Object $enabledPacks = this.getEnabledPacks();
		result = result * PRIME + ($enabledPacks == null ? 43 : $enabledPacks.hashCode());
		final Object $config = this.getConfig();
		result = result * PRIME + ($config == null ? 43 : $config.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ArtifactAppRequestDTO(code=" + this.getCode() + ", name=" + this.getName() + ", description=" + this.getDescription() + ", status=" + this.getStatus() + ", generatorLanguage=" + this.getGeneratorLanguage() + ", buildTool=" + this.getBuildTool() + ", enabledPacks=" + this.getEnabledPacks() + ", config=" + this.getConfig() + ")";
	}
}
