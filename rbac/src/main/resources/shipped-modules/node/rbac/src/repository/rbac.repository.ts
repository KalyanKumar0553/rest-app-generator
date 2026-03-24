import { prisma } from '../../../../src/lib/prisma';

import type { RoleModel, UserRoleModel } from '../model/rbac.model';

export class RbacRepository {
  async seedRoles(roles: string[], permissions: string[]): Promise<void> {
    for (const roleName of roles) {
      await prisma.role.upsert({
        where: {
          name: roleName
        },
        update: {},
        create: {
          name: roleName
        }
      });
    }

    for (const permissionName of permissions) {
      await prisma.permission.upsert({
        where: {
          name: permissionName
        },
        update: {},
        create: {
          name: permissionName
        }
      });
    }

    for (const roleName of roles) {
      const role = await prisma.role.findUnique({ where: { name: roleName } });
      if (!role) {
        continue;
      }
      for (const permissionName of permissions) {
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
      name: role.name,
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
