package com.src.main.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.model.workflow.WorkflowDefinitionEntity;

public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinitionEntity, UUID> {

	Optional<WorkflowDefinitionEntity> findByLanguageAndActiveTrue(String language);

	List<WorkflowDefinitionEntity> findByActiveTrue();
}
