package com.src.main.repository;

import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.model.ProjectEntity;

public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {

	List<ProjectEntity> findByOwnerId(String ownerId);

}
