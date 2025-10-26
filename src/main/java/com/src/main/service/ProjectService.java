package com.src.main.service;

import org.springframework.http.ResponseEntity;

import com.src.main.dto.*;
import java.util.*;
import java.util.UUID;

public interface ProjectService {
	ProjectCreateResponse create(String yamlText);

	ProjectStatusResponse status(UUID id);

	ResponseEntity<byte[]> download(UUID id);

	List<ProjectSummary> list();
}