package com.src.main.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDraftTabDataDTO {
	private String tabKey;
	private Map<String, Object> tabData;
}
