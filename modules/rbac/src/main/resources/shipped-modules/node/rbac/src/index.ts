import type { Express, Request, Response, NextFunction } from 'express';

import { resolveRbacConfig } from './config/rbac-config';
import { RbacController } from './controller/rbac.controller';
import { RbacRepository } from './repository/rbac.repository';
import { buildRbacRouter } from './route/rbac.routes';
import { RbacService } from './service/rbac.service';

type ModuleManifest = {
  generator: string;
  selectedModules: string[];
  moduleConfigs?: Record<string, Record<string, unknown>>;
};

export const requireRole = (allowedRoles: string[]) => {
  return (request: Request, response: Response, next: NextFunction): void => {
    const currentRole = String(request.header('x-role') ?? 'VIEWER').toUpperCase();
    if (!allowedRoles.map((role) => role.toUpperCase()).includes(currentRole)) {
      response.status(403).json({
        message: 'RBAC policy denied this request.',
        requiredRoles: allowedRoles
      });
      return;
    }
    next();
  };
};

export const registerModule = (
  app: Express,
  config: Record<string, unknown>,
  _manifest: ModuleManifest
): void => {
  const resolvedConfig = resolveRbacConfig(config);
  const rbacRepository = new RbacRepository();
  const rbacService = new RbacService(resolvedConfig, rbacRepository);
  const rbacController = new RbacController(rbacService);
  app.use('/api/modules/rbac', buildRbacRouter(rbacController));
};
