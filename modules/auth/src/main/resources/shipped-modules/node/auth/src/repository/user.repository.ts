import { prisma } from '../../../../src/lib/prisma';

import type { UserCreateModel, UserModel } from '../model/user.model';

export class UserRepository {
  async findByUsername(username: string): Promise<UserModel | null> {
    const user = await prisma.user.findUnique({
      where: {
        username: username.toLowerCase()
      }
    });
    if (!user) {
      return null;
    }
    return {
      id: user.id,
      username: user.username,
      email: user.email,
      passwordHash: user.passwordHash,
      status: user.status as UserModel['status'],
      timezone: user.timezone ?? undefined
    };
  }

  async save(user: UserCreateModel): Promise<UserModel> {
    const saved = await prisma.user.upsert({
      where: {
        username: user.username.toLowerCase()
      },
      update: {
        email: user.email,
        passwordHash: user.passwordHash,
        status: user.status,
        timezone: user.timezone ?? null
      },
      create: {
        username: user.username.toLowerCase(),
        email: user.email,
        passwordHash: user.passwordHash,
        status: user.status,
        timezone: user.timezone ?? null
      }
    });
    return {
      id: saved.id,
      username: saved.username,
      email: saved.email,
      passwordHash: saved.passwordHash,
      status: saved.status as UserModel['status'],
      timezone: saved.timezone ?? undefined
    };
  }

  async findAll(): Promise<UserModel[]> {
    const users = await prisma.user.findMany({
      orderBy: {
        createdAt: 'desc'
      }
    });
    return users.map((user) => ({
      id: user.id,
      username: user.username,
      email: user.email,
      passwordHash: user.passwordHash,
      status: user.status as UserModel['status'],
      timezone: user.timezone ?? undefined
    }));
  }
}
