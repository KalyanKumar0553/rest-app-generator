package com.src.main.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppSpecDTO {
	private String basePackage;
	private String packages;
	private Boolean enableOpenapi;
	private Boolean enableLombok;
	private Boolean useDockerCompose;
	private Boolean pluralizeTableNames;
	private List<String> profiles;
	private List<ModelSpecDTO> models;
	private List<EnumSpecDTO> enums;

	public String getBasePackage() {
		return this.basePackage;
	}

	public String getPackages() {
		return this.packages;
	}

	public Boolean getEnableOpenapi() {
		return this.enableOpenapi;
	}

	public Boolean getEnableLombok() {
		return this.enableLombok;
	}

	public Boolean getUseDockerCompose() {
		return this.useDockerCompose;
	}

	public Boolean getPluralizeTableNames() {
		return this.pluralizeTableNames;
	}

	public List<String> getProfiles() {
		return this.profiles;
	}

	public List<ModelSpecDTO> getModels() {
		return this.models;
	}

	public List<EnumSpecDTO> getEnums() {
		return this.enums;
	}

	public void setBasePackage(final String basePackage) {
		this.basePackage = basePackage;
	}

	public void setPackages(final String packages) {
		this.packages = packages;
	}

	public void setEnableOpenapi(final Boolean enableOpenapi) {
		this.enableOpenapi = enableOpenapi;
	}

	public void setEnableLombok(final Boolean enableLombok) {
		this.enableLombok = enableLombok;
	}

	public void setUseDockerCompose(final Boolean useDockerCompose) {
		this.useDockerCompose = useDockerCompose;
	}

	public void setPluralizeTableNames(final Boolean pluralizeTableNames) {
		this.pluralizeTableNames = pluralizeTableNames;
	}

	public void setProfiles(final List<String> profiles) {
		this.profiles = profiles;
	}

	public void setModels(final List<ModelSpecDTO> models) {
		this.models = models;
	}

	public void setEnums(final List<EnumSpecDTO> enums) {
		this.enums = enums;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof AppSpecDTO)) return false;
		final AppSpecDTO other = (AppSpecDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$enableOpenapi = this.getEnableOpenapi();
		final Object other$enableOpenapi = other.getEnableOpenapi();
		if (this$enableOpenapi == null ? other$enableOpenapi != null : !this$enableOpenapi.equals(other$enableOpenapi)) return false;
		final Object this$enableLombok = this.getEnableLombok();
		final Object other$enableLombok = other.getEnableLombok();
		if (this$enableLombok == null ? other$enableLombok != null : !this$enableLombok.equals(other$enableLombok)) return false;
		final Object this$useDockerCompose = this.getUseDockerCompose();
		final Object other$useDockerCompose = other.getUseDockerCompose();
		if (this$useDockerCompose == null ? other$useDockerCompose != null : !this$useDockerCompose.equals(other$useDockerCompose)) return false;
		final Object this$pluralizeTableNames = this.getPluralizeTableNames();
		final Object other$pluralizeTableNames = other.getPluralizeTableNames();
		if (this$pluralizeTableNames == null ? other$pluralizeTableNames != null : !this$pluralizeTableNames.equals(other$pluralizeTableNames)) return false;
		final Object this$basePackage = this.getBasePackage();
		final Object other$basePackage = other.getBasePackage();
		if (this$basePackage == null ? other$basePackage != null : !this$basePackage.equals(other$basePackage)) return false;
		final Object this$packages = this.getPackages();
		final Object other$packages = other.getPackages();
		if (this$packages == null ? other$packages != null : !this$packages.equals(other$packages)) return false;
		final Object this$profiles = this.getProfiles();
		final Object other$profiles = other.getProfiles();
		if (this$profiles == null ? other$profiles != null : !this$profiles.equals(other$profiles)) return false;
		final Object this$models = this.getModels();
		final Object other$models = other.getModels();
		if (this$models == null ? other$models != null : !this$models.equals(other$models)) return false;
		final Object this$enums = this.getEnums();
		final Object other$enums = other.getEnums();
		if (this$enums == null ? other$enums != null : !this$enums.equals(other$enums)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof AppSpecDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $enableOpenapi = this.getEnableOpenapi();
		result = result * PRIME + ($enableOpenapi == null ? 43 : $enableOpenapi.hashCode());
		final Object $enableLombok = this.getEnableLombok();
		result = result * PRIME + ($enableLombok == null ? 43 : $enableLombok.hashCode());
		final Object $useDockerCompose = this.getUseDockerCompose();
		result = result * PRIME + ($useDockerCompose == null ? 43 : $useDockerCompose.hashCode());
		final Object $pluralizeTableNames = this.getPluralizeTableNames();
		result = result * PRIME + ($pluralizeTableNames == null ? 43 : $pluralizeTableNames.hashCode());
		final Object $basePackage = this.getBasePackage();
		result = result * PRIME + ($basePackage == null ? 43 : $basePackage.hashCode());
		final Object $packages = this.getPackages();
		result = result * PRIME + ($packages == null ? 43 : $packages.hashCode());
		final Object $profiles = this.getProfiles();
		result = result * PRIME + ($profiles == null ? 43 : $profiles.hashCode());
		final Object $models = this.getModels();
		result = result * PRIME + ($models == null ? 43 : $models.hashCode());
		final Object $enums = this.getEnums();
		result = result * PRIME + ($enums == null ? 43 : $enums.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "AppSpecDTO(basePackage=" + this.getBasePackage() + ", packages=" + this.getPackages() + ", enableOpenapi=" + this.getEnableOpenapi() + ", enableLombok=" + this.getEnableLombok() + ", useDockerCompose=" + this.getUseDockerCompose() + ", pluralizeTableNames=" + this.getPluralizeTableNames() + ", profiles=" + this.getProfiles() + ", models=" + this.getModels() + ", enums=" + this.getEnums() + ")";
	}

	public AppSpecDTO(final String basePackage, final String packages, final Boolean enableOpenapi, final Boolean enableLombok, final Boolean useDockerCompose, final Boolean pluralizeTableNames, final List<String> profiles, final List<ModelSpecDTO> models, final List<EnumSpecDTO> enums) {
		this.basePackage = basePackage;
		this.packages = packages;
		this.enableOpenapi = enableOpenapi;
		this.enableLombok = enableLombok;
		this.useDockerCompose = useDockerCompose;
		this.pluralizeTableNames = pluralizeTableNames;
		this.profiles = profiles;
		this.models = models;
		this.enums = enums;
	}

	public AppSpecDTO() {
	}
}
