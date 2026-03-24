import type { NextFunction, Request, Response } from 'express';

import { verifyJwtToken } from '../util/jwt.util';

export const jwtAuthenticationMiddleware = (secret: string) => {
  return (request: Request, response: Response, next: NextFunction): void => {
    const authHeader = request.header('authorization') ?? '';
    const token = authHeader.startsWith('Bearer ') ? authHeader.slice(7) : '';
    if (!token) {
      response.status(401).json({ message: 'Missing bearer token.' });
      return;
    }

    try {
      (request as Request & { user?: Record<string, unknown> }).user = verifyJwtToken(token, secret);
      next();
    } catch (_error) {
      response.status(401).json({ message: 'Invalid bearer token.' });
    }
  };
};
