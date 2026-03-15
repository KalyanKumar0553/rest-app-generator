package com.src.main.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.model.ProjectContributorEntity;

public interface ProjectContributorRepository extends JpaRepository<ProjectContributorEntity, UUID> {
	boolean existsByProjectIdAndUserId(UUID projectId, String userId);
	List<ProjectContributorEntity> findByProjectIdOrderByCreatedAtAsc(UUID projectId);
	long deleteByProjectIdAndUserId(UUID projectId, String userId);
}
