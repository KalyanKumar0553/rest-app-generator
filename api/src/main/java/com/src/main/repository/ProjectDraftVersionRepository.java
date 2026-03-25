package com.src.main.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.model.ProjectDraftVersionEntity;

public interface ProjectDraftVersionRepository extends JpaRepository<ProjectDraftVersionEntity, UUID> {

	List<ProjectDraftVersionEntity> findByProjectIdOrderByDraftVersionDesc(UUID projectId);

	Optional<ProjectDraftVersionEntity> findByIdAndProjectId(UUID id, UUID projectId);
}
