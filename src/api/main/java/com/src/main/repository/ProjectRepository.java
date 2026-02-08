package com.src.main.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.model.ProjectEntity;

public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {


}
