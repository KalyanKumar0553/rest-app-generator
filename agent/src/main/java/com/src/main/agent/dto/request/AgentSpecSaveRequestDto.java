package com.src.main.agent.dto.request;

import java.util.Map;

public class AgentSpecSaveRequestDto {

	private Map<String, Object> specOverrides;

	public AgentSpecSaveRequestDto() {
	}

	public AgentSpecSaveRequestDto(Map<String, Object> specOverrides) {
		this.specOverrides = specOverrides;
	}

	public Map<String, Object> getSpecOverrides() {
		return specOverrides;
	}

	public void setSpecOverrides(Map<String, Object> specOverrides) {
		this.specOverrides = specOverrides;
	}
}
