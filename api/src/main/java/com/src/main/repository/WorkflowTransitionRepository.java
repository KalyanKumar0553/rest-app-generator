package com.src.main.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.model.workflow.WorkflowTransitionEntity;

public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransitionEntity, UUID> {

	List<WorkflowTransitionEntity> findByWorkflowStepIdInOrderByPriorityAsc(Collection<UUID> stepIds);
}
