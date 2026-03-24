package com.src.main.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.model.ProjectCollaborationRequestEntity;

public interface ProjectCollaborationRequestRepository extends JpaRepository<ProjectCollaborationRequestEntity, UUID> {
	List<ProjectCollaborationRequestEntity> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
	Optional<ProjectCollaborationRequestEntity> findByIdAndProjectId(UUID id, UUID projectId);
	Optional<ProjectCollaborationRequestEntity> findFirstByProjectIdAndRequesterIdOrderByCreatedAtDesc(UUID projectId, String requesterId);
}
