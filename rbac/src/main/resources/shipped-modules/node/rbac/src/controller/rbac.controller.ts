import type { Request, Response } from 'express';

import { RbacService } from '../service/rbac.service';

export class RbacController {
  constructor(private readonly rbacService: RbacService) {}

  roles = async (_request: Request, response: Response): Promise<void> => {
    response.json(await this.rbacService.listRoles());
  };

  assignRole = async (request: Request, response: Response): Promise<void> => {
    const userId = String(request.body?.userId ?? '').trim();
    const role = String(request.body?.role ?? '').trim();
    if (!userId || !role) {
      response.status(400).json({ message: 'userId and role are required.' });
      return;
    }
    response.json(await this.rbacService.assignRole(userId, role));
  };
}
