package com.src.main.cdn.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;

public class CdnMediaBulkDeleteRequestDTO {

	@NotEmpty
	private List<UUID> ids;

	public List<UUID> getIds() {
		return ids;
	}

	public void setIds(List<UUID> ids) {
		this.ids = ids;
	}
}
