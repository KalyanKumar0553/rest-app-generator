package com.src.main.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.src.main.model.ProjectEntity;
import com.src.main.model.ProjectRunEntity;

public interface ProjectOrchestrationService {

	ProjectEntity updateSpec(UUID projectId, String ownerId, String yamlContent);

	ProjectRunEntity updateSpecAndGenerate(UUID projectId, String ownerId, String yamlContent);

	ProjectRunEntity generateCode(UUID projectId, String ownerId);

	ProjectEntity getOwnedProject(UUID projectId, String ownerId);

	List<ProjectRunEntity> getRunsForProject(UUID projectId, String ownerId);

	ProjectRunEntity getRun(UUID runId, String ownerId);
	
	ResponseEntity<byte[]> download(UUID id);
}
