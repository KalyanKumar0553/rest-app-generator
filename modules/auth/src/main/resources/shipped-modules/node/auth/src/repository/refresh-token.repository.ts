import { prisma } from '../../../../src/lib/prisma';

import type { RefreshTokenSession } from '../model/auth-session.model';

export class RefreshTokenRepository {
  async save(session: RefreshTokenSession, userId: string): Promise<RefreshTokenSession> {
    const saved = await prisma.refreshToken.upsert({
      where: {
        token: session.refreshToken
      },
      update: {
        subject: session.subject,
        issuedAt: new Date(session.issuedAt),
        expiresAt: new Date(session.expiresAt),
        userId
      },
      create: {
        token: session.refreshToken,
        subject: session.subject,
        issuedAt: new Date(session.issuedAt),
        expiresAt: new Date(session.expiresAt),
        userId
      }
    });
    return {
      subject: saved.subject,
      refreshToken: saved.token,
      issuedAt: saved.issuedAt.toISOString(),
      expiresAt: saved.expiresAt.toISOString()
    };
  }

  async findByToken(refreshToken: string): Promise<RefreshTokenSession | null> {
    const saved = await prisma.refreshToken.findUnique({
      where: {
        token: refreshToken
      }
    });
    if (!saved) {
      return null;
    }
    return {
      subject: saved.subject,
      refreshToken: saved.token,
      issuedAt: saved.issuedAt.toISOString(),
      expiresAt: saved.expiresAt.toISOString()
    };
  }

  async delete(refreshToken: string): Promise<void> {
    await prisma.refreshToken.deleteMany({
      where: {
        token: refreshToken
      }
    });
  }
}
