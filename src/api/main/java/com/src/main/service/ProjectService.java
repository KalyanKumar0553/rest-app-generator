package com.src.main.service;

import java.util.List;

import com.src.main.dto.ProjectCreateResponseDTO;
import com.src.main.dto.ProjectSummaryDTO;

public interface ProjectService {
	
	List<ProjectSummaryDTO> list();
	
	ProjectCreateResponseDTO create(String yamlText);
}