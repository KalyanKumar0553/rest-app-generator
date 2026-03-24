package com.src.main.workflow.engine;

import java.util.List;

import org.springframework.stereotype.Service;

import com.src.main.model.workflow.WorkflowDefinitionEntity;
import com.src.main.model.workflow.WorkflowStepEntity;
import com.src.main.model.workflow.WorkflowTransitionEntity;
import com.src.main.repository.WorkflowDefinitionRepository;
import com.src.main.repository.WorkflowStepRepository;
import com.src.main.repository.WorkflowTransitionRepository;
import com.src.main.sm.executor.common.GenerationLanguage;

@Service
public class WorkflowDefinitionService {

	private final WorkflowDefinitionRepository workflowDefinitionRepository;
	private final WorkflowStepRepository workflowStepRepository;
	private final WorkflowTransitionRepository workflowTransitionRepository;

	public WorkflowDefinitionService(
			WorkflowDefinitionRepository workflowDefinitionRepository,
			WorkflowStepRepository workflowStepRepository,
			WorkflowTransitionRepository workflowTransitionRepository) {
		this.workflowDefinitionRepository = workflowDefinitionRepository;
		this.workflowStepRepository = workflowStepRepository;
		this.workflowTransitionRepository = workflowTransitionRepository;
	}

	public WorkflowPlan loadActivePlan(GenerationLanguage language) {
		WorkflowDefinitionEntity definition = workflowDefinitionRepository.findByLanguageAndActiveTrue(language.name())
				.orElseThrow(() -> new IllegalArgumentException("No active workflow definition for language " + language));
		List<WorkflowStepEntity> steps = workflowStepRepository.findByWorkflowIdAndEnabledTrueOrderByStepOrderAsc(definition.getId());
		List<WorkflowTransitionEntity> transitions = workflowTransitionRepository.findByWorkflowStepIdInOrderByPriorityAsc(
				steps.stream().map(WorkflowStepEntity::getId).toList());
		return WorkflowPlan.of(definition, steps, transitions);
	}

	public List<WorkflowPlan> loadAllActivePlans() {
		return workflowDefinitionRepository.findByActiveTrue().stream()
				.map(definition -> {
					List<WorkflowStepEntity> steps = workflowStepRepository.findByWorkflowIdAndEnabledTrueOrderByStepOrderAsc(definition.getId());
					List<WorkflowTransitionEntity> transitions = workflowTransitionRepository.findByWorkflowStepIdInOrderByPriorityAsc(
							steps.stream().map(WorkflowStepEntity::getId).toList());
					return WorkflowPlan.of(definition, steps, transitions);
				})
				.toList();
	}
}
