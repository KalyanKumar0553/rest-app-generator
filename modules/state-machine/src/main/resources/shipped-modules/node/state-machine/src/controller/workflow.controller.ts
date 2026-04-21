import type { Request, Response } from 'express';

import { WorkflowService } from '../service/workflow.service';

export class WorkflowController {
  constructor(private readonly workflowService: WorkflowService) {}

  summary = async (_request: Request, response: Response): Promise<void> => {
    response.json(await this.workflowService.summary());
  };
}
