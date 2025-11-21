package com.src.main.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.src.main.dto.ProjectCreateResponseDTO;
import com.src.main.dto.ProjectStatusResponseDTO;
import com.src.main.dto.ProjectSummaryDTO;

public interface ProjectService {
	ProjectCreateResponseDTO create(String yamlText);

	ProjectStatusResponseDTO status(UUID id);

	ResponseEntity<byte[]> download(UUID id);

	List<ProjectSummaryDTO> list();
}