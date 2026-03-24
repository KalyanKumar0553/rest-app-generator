package com.src.main.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.model.workflow.WorkflowExecutorPoolEntity;

public interface WorkflowExecutorPoolRepository extends JpaRepository<WorkflowExecutorPoolEntity, UUID> {

	List<WorkflowExecutorPoolEntity> findByActiveTrue();

	Optional<WorkflowExecutorPoolEntity> findByPoolCodeAndActiveTrue(String poolCode);
}
