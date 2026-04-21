import type { Express } from 'express';

import { resolveWorkflowConfig } from './config/workflow-config';
import { WorkflowController } from './controller/workflow.controller';
import { WorkflowRepository } from './repository/workflow.repository';
import { buildWorkflowRouter } from './route/workflow.routes';
import { WorkflowService } from './service/workflow.service';

type ModuleManifest = {
  generator: string;
  selectedModules: string[];
  moduleConfigs?: Record<string, Record<string, unknown>>;
};

export const registerModule = (
  app: Express,
  config: Record<string, unknown>,
  _manifest: ModuleManifest
): void => {
  const resolvedConfig = resolveWorkflowConfig(config);
  const workflowRepository = new WorkflowRepository();
  const workflowService = new WorkflowService(resolvedConfig, workflowRepository);
  const workflowController = new WorkflowController(workflowService);
  app.use('/api/modules/state-machine', buildWorkflowRouter(workflowController));
};
