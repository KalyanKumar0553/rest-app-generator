import jwt from 'jsonwebtoken';

export const signJwtToken = (
  claims: Record<string, unknown>,
  secret: string,
  expiresIn: string
): string => jwt.sign(claims, secret, { expiresIn });

export const verifyJwtToken = (token: string, secret: string): Record<string, unknown> =>
  jwt.verify(token, secret) as Record<string, unknown>;
