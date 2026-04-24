import type { Request, Response } from 'express';

import { WorkflowService } from '../service/workflow.service';

export class WorkflowController {
  constructor(private readonly workflowService: WorkflowService) {}

  summary = async (_request: Request, response: Response): Promise<void> => {
    response.json(await this.workflowService.summary());
  };

  createInstance = async (request: Request, response: Response): Promise<void> => {
    const entityId = String(request.body?.entityId ?? '').trim();
    if (!entityId) {
      response.status(400).json({ message: 'entityId is required.' });
      return;
    }
    response.status(201).json(await this.workflowService.createInstance(entityId));
  };

  transition = async (request: Request, response: Response): Promise<void> => {
    const entityId = String(request.params.entityId ?? '').trim();
    const eventName = String(request.body?.event ?? '').trim();
    if (!entityId || !eventName) {
      response.status(400).json({ message: 'entityId and event are required.' });
      return;
    }
    response.json(await this.workflowService.transition(entityId, eventName));
  };
}
