export type RbacModuleConfig = {
  roles?: Array<{
    code: string;
    displayName?: string;
    description?: string;
    systemRole?: boolean;
    active?: boolean;
  }>;
  permissions?: Array<{
    code: string;
    displayName?: string;
    description?: string;
    category?: string;
    active?: boolean;
  }>;
  rolePermissions?: Array<{
    roleCode: string;
    permissionCodes: string[];
  }>;
  routes?: Array<{
    pathPattern: string;
    httpMethod?: string | null;
    authorities: string[];
    priority?: number;
    active?: boolean;
  }>;
  defaultRole?: string;
};

export const resolveRbacConfig = (config: Record<string, unknown>): Required<RbacModuleConfig> => ({
  roles: Array.isArray(config.roles) && config.roles.length
    ? config.roles.map((role) => ({
        code: String((role as Record<string, unknown>).code ?? '').trim().toUpperCase(),
        displayName: String((role as Record<string, unknown>).displayName ?? '').trim() || undefined,
        description: String((role as Record<string, unknown>).description ?? '').trim() || undefined,
        systemRole: typeof (role as Record<string, unknown>).systemRole === 'boolean'
          ? Boolean((role as Record<string, unknown>).systemRole)
          : false,
        active: typeof (role as Record<string, unknown>).active === 'boolean'
          ? Boolean((role as Record<string, unknown>).active)
          : true
      })).filter((role) => role.code)
    : [
        { code: 'ROLE_USER', displayName: 'User', description: 'Standard authenticated access.', systemRole: true, active: true },
        { code: 'ROLE_ADMIN', displayName: 'Admin', description: 'Administrative access.', systemRole: true, active: true }
      ],
  permissions: Array.isArray(config.permissions) && config.permissions.length
    ? config.permissions.map((permission) => ({
        code: String((permission as Record<string, unknown>).code ?? '').trim(),
        displayName: String((permission as Record<string, unknown>).displayName ?? '').trim() || undefined,
        description: String((permission as Record<string, unknown>).description ?? '').trim() || undefined,
        category: String((permission as Record<string, unknown>).category ?? 'RBAC').trim() || 'RBAC',
        active: typeof (permission as Record<string, unknown>).active === 'boolean'
          ? Boolean((permission as Record<string, unknown>).active)
          : true
      })).filter((permission) => permission.code)
    : [
        { code: 'project.read', displayName: 'View Projects', description: 'Read project details.', category: 'PROJECT', active: true },
        { code: 'project.manage', displayName: 'Manage Projects', description: 'Create and update projects.', category: 'PROJECT', active: true }
      ],
  rolePermissions: Array.isArray(config.rolePermissions) && config.rolePermissions.length
    ? config.rolePermissions.map((mapping) => ({
        roleCode: String((mapping as Record<string, unknown>).roleCode ?? '').trim().toUpperCase(),
        permissionCodes: Array.isArray((mapping as Record<string, unknown>).permissionCodes)
          ? ((mapping as Record<string, unknown>).permissionCodes as unknown[])
              .map((code) => String(code ?? '').trim())
              .filter(Boolean)
          : []
      })).filter((mapping) => mapping.roleCode && mapping.permissionCodes.length)
    : [
        { roleCode: 'ROLE_USER', permissionCodes: ['project.read'] },
        { roleCode: 'ROLE_ADMIN', permissionCodes: ['project.read', 'project.manage'] }
      ],
  routes: Array.isArray(config.routes) && config.routes.length
    ? config.routes.map((route) => ({
        pathPattern: String((route as Record<string, unknown>).pathPattern ?? '').trim(),
        httpMethod: String((route as Record<string, unknown>).httpMethod ?? '').trim().toUpperCase() || null,
        authorities: Array.isArray((route as Record<string, unknown>).authorities)
          ? ((route as Record<string, unknown>).authorities as unknown[]).map((authority) => String(authority ?? '').trim()).filter(Boolean)
          : [],
        priority: Number((route as Record<string, unknown>).priority ?? 100),
        active: typeof (route as Record<string, unknown>).active === 'boolean'
          ? Boolean((route as Record<string, unknown>).active)
          : true
      })).filter((route) => route.pathPattern && route.authorities.length)
    : [],
  defaultRole: typeof config.defaultRole === 'string' && config.defaultRole.trim()
    ? config.defaultRole.trim().toUpperCase()
    : 'ROLE_USER'
});
