package com.src.main.cdn.dto;

import jakarta.validation.constraints.NotNull;

public class CdnImageBatchToggleRequestDTO {

	@NotNull(message = "enabled is required")
	private Boolean enabled;

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
}
