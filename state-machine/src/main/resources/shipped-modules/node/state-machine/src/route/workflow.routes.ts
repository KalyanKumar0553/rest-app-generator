import { Router } from 'express';

import { WorkflowController } from '../controller/workflow.controller';

export const buildWorkflowRouter = (workflowController: WorkflowController): Router => {
  const router = Router();
  router.get('/workflow', workflowController.summary);
  return router;
};
