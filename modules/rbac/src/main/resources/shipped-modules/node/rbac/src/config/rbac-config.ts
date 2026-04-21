export type RbacModuleConfig = {
  roles?: string[];
  permissions?: string[];
  defaultRole?: string;
};

export const resolveRbacConfig = (config: Record<string, unknown>): Required<RbacModuleConfig> => ({
  roles: Array.isArray(config.roles) && config.roles.length
    ? config.roles.map((role) => String(role))
    : ['ADMIN', 'EDITOR', 'VIEWER'],
  permissions: Array.isArray(config.permissions) && config.permissions.length
    ? config.permissions.map((permission) => String(permission))
    : ['project.read', 'project.write'],
  defaultRole: typeof config.defaultRole === 'string' && config.defaultRole.trim()
    ? config.defaultRole.trim()
    : 'VIEWER'
});
