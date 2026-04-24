import { prisma } from '../../../../src/lib/prisma';

import type { RoleModel, UserRoleModel } from '../model/rbac.model';
import type { RbacModuleConfig } from '../config/rbac-config';

export class RbacRepository {
  async seedRoles(config: Required<RbacModuleConfig>): Promise<void> {
    for (const role of config.roles) {
      await prisma.role.upsert({
        where: {
          name: role.code
        },
        update: {
          displayName: role.displayName ?? role.code,
          description: role.description ?? null,
          systemRole: role.systemRole ?? false,
          active: role.active ?? true
        },
        create: {
          name: role.code,
          displayName: role.displayName ?? role.code,
          description: role.description ?? null,
          systemRole: role.systemRole ?? false,
          active: role.active ?? true
        }
      });
    }

    for (const permission of config.permissions) {
      await prisma.permission.upsert({
        where: {
          name: permission.code
        },
        update: {
          displayName: permission.displayName ?? permission.code,
          description: permission.description ?? null,
          category: permission.category ?? 'RBAC',
          active: permission.active ?? true
        },
        create: {
          name: permission.code,
          displayName: permission.displayName ?? permission.code,
          description: permission.description ?? null,
          category: permission.category ?? 'RBAC',
          active: permission.active ?? true
        }
      });
    }

    for (const mapping of config.rolePermissions) {
      const role = await prisma.role.findUnique({ where: { name: mapping.roleCode } });
      if (!role) {
        continue;
      }
      for (const permissionName of mapping.permissionCodes) {
        const permission = await prisma.permission.findUnique({ where: { name: permissionName } });
        if (!permission) {
          continue;
        }
        await prisma.rolePermission.upsert({
          where: {
            roleId_permissionId: {
              roleId: role.id,
              permissionId: permission.id
            }
          },
          update: {},
          create: {
            roleId: role.id,
            permissionId: permission.id
          }
        });
      }
    }
  }

  async listRoles(): Promise<RoleModel[]> {
    const roles = await prisma.role.findMany({
      include: {
        rolePermissions: {
          include: {
            permission: true
          }
        }
      },
      orderBy: {
        name: 'asc'
      }
    });
    return roles.map((role) => ({
      code: role.name,
      displayName: role.displayName ?? undefined,
      description: role.description ?? undefined,
      systemRole: role.systemRole,
      active: role.active,
      permissions: role.rolePermissions.map((mapping) => mapping.permission.name)
    }));
  }

  async assignRole(userId: string, roleName: string): Promise<UserRoleModel> {
    const role = await prisma.role.findUnique({
      where: {
        name: roleName
      }
    });
    if (!role) {
      throw new Error(`Role ${roleName} not found.`);
    }
    const userRole = await prisma.userRole.upsert({
      where: {
        userId: userId
      },
      update: {
        roleId: role.id
      },
      create: {
        userId,
        roleId: role.id
      },
      include: {
        role: true
      }
    });
    return {
      userId: userRole.userId,
      role: userRole.role.name
    };
  }

  async currentRole(userId: string): Promise<string | null> {
    const userRole = await prisma.userRole.findUnique({
      where: {
        userId
      },
      include: {
        role: true
      }
    });
    return userRole?.role.name ?? null;
  }
}
