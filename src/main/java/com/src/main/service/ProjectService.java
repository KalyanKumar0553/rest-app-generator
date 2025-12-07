package com.src.main.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.src.main.dto.JSONResponseDTO;
import com.src.main.dto.ProjectCreateResponseDTO;
import com.src.main.dto.ProjectSummaryDTO;

public interface ProjectService {
	
	ResponseEntity<JSONResponseDTO<List<ProjectSummaryDTO>>> fetchProjecs(String ownerId);
	
	ProjectCreateResponseDTO create(String yamlText, String ownerId);
} 
