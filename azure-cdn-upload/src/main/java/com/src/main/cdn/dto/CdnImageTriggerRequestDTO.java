package com.src.main.cdn.dto;

import java.util.List;
import java.util.UUID;

public class CdnImageTriggerRequestDTO {

	private List<UUID> draftIds;

	public List<UUID> getDraftIds() {
		return draftIds;
	}

	public void setDraftIds(List<UUID> draftIds) {
		this.draftIds = draftIds;
	}
}
