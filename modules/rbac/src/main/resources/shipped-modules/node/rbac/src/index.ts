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

const toRegex = (pathPattern: string): RegExp => {
  const escaped = pathPattern.replace(/[.+?^${}()|[\]\\]/g, '\\$&');
  return new RegExp(`^${escaped.replace(/\\\*\\\*/g, '.*').replace(/\\\*/g, '[^/]*')}$`);
};

const installRouteProtection = (
  app: Express,
  config: ReturnType<typeof resolveRbacConfig>
): void => {
  const permissionMap = new Map<string, string[]>(
    config.rolePermissions.map((mapping) => [mapping.roleCode, mapping.permissionCodes])
  );
  const policies = config.routes
    .filter((route) => route.active !== false)
    .sort((left, right) => Number(left.priority ?? 100) - Number(right.priority ?? 100))
    .map((route) => ({
      ...route,
      matcher: toRegex(route.pathPattern)
    }));
  if (!policies.length) {
    return;
  }
  app.use((request: Request, response: Response, next: NextFunction): void => {
    const requestMethod = String(request.method ?? '').trim().toUpperCase();
    const requestPath = request.path;
    const matchedPolicy = policies.find((policy) => {
      const methodMatches = !policy.httpMethod || policy.httpMethod === requestMethod;
      return methodMatches && policy.matcher.test(requestPath);
    });
    if (!matchedPolicy) {
      next();
      return;
    }
    const role = String(request.header('x-role') ?? config.defaultRole).trim().toUpperCase();
    const directAuthorities = String(request.header('x-authorities') ?? '')
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean);
    const inheritedAuthorities = permissionMap.get(role) ?? [];
    const authorities = new Set([...directAuthorities, ...inheritedAuthorities]);
    const isAllowed = matchedPolicy.authorities.some((authority) => authorities.has(authority));
    if (!isAllowed) {
      response.status(403).json({
        message: 'RBAC authority denied this request.',
        requiredAuthorities: matchedPolicy.authorities,
        pathPattern: matchedPolicy.pathPattern,
        httpMethod: matchedPolicy.httpMethod ?? null
      });
      return;
    }
    next();
  });
};

export const registerModule = (
  app: Express,
  config: Record<string, unknown>,
  _manifest: ModuleManifest
): void => {
  const resolvedConfig = resolveRbacConfig(config);
  installRouteProtection(app, resolvedConfig);
  const rbacRepository = new RbacRepository();
  const rbacService = new RbacService(resolvedConfig, rbacRepository);
  const rbacController = new RbacController(rbacService);
  app.use('/api/modules/rbac', buildRbacRouter(rbacController));
};
