package com.src.main.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.model.ProjectContributorEntity;

public interface ProjectContributorRepository extends JpaRepository<ProjectContributorEntity, UUID> {
	boolean existsByProjectIdAndUserId(UUID projectId, String userId);
	boolean existsByProjectIdAndUserIdIn(UUID projectId, Collection<String> userIds);
	List<ProjectContributorEntity> findByProjectIdOrderByCreatedAtAsc(UUID projectId);
	long deleteByProjectId(UUID projectId);
	long deleteByProjectIdAndUserId(UUID projectId, String userId);
}
