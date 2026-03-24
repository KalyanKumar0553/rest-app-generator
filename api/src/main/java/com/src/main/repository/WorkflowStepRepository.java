package com.src.main.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.model.workflow.WorkflowStepEntity;

public interface WorkflowStepRepository extends JpaRepository<WorkflowStepEntity, UUID> {

	List<WorkflowStepEntity> findByWorkflowIdAndEnabledTrueOrderByStepOrderAsc(UUID workflowId);
}
