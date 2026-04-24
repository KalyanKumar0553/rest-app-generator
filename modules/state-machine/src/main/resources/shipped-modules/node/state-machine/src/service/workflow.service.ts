import type { WorkflowModuleConfig } from '../config/workflow-config';
import type { WorkflowInstanceModel, WorkflowStateModel, WorkflowTransitionModel } from '../model/workflow.model';
import { WorkflowRepository } from '../repository/workflow.repository';

export class WorkflowService {
  constructor(
    private readonly config: Required<WorkflowModuleConfig>,
    private readonly workflowRepository: WorkflowRepository
  ) {}

  async ensureSeedData(): Promise<void> {
    await this.workflowRepository.seedWorkflow(this.config.workflowName, this.config.states);
    await this.workflowRepository.seedTransitions(this.config.workflowName, this.config.transitions);
  }

  async summary(): Promise<{
    workflowName: string;
    initialState: string;
    states: WorkflowStateModel[];
    transitions: WorkflowTransitionModel[];
  }> {
    await this.ensureSeedData();
    const summary = await this.workflowRepository.summary(this.config.workflowName);
    return {
      workflowName: summary.workflowName,
      initialState: this.config.initialState,
      states: summary.states,
      transitions: summary.transitions
    };
  }

  async createInstance(entityId: string): Promise<WorkflowInstanceModel> {
    await this.ensureSeedData();
    return this.workflowRepository.createInstance(this.config.workflowName, entityId, this.config.initialState);
  }

  async transition(entityId: string, eventName: string): Promise<WorkflowInstanceModel> {
    await this.ensureSeedData();
    return this.workflowRepository.transition(this.config.workflowName, entityId, eventName);
  }
}
