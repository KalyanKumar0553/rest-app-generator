import type { WorkflowModuleConfig } from '../config/workflow-config';
import type { WorkflowStateModel } from '../model/workflow.model';
import { WorkflowRepository } from '../repository/workflow.repository';

export class WorkflowService {
  constructor(
    private readonly config: Required<WorkflowModuleConfig>,
    private readonly workflowRepository: WorkflowRepository
  ) {}

  async ensureSeedData(): Promise<void> {
    await this.workflowRepository.seedWorkflow(this.config.workflowName, this.config.states);
  }

  async summary(): Promise<{ workflowName: string; states: WorkflowStateModel[] }> {
    await this.ensureSeedData();
    return this.workflowRepository.summary(this.config.workflowName);
  }
}
