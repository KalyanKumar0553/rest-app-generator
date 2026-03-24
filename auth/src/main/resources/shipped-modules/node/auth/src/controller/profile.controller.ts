import type { Request, Response } from 'express';

import { AuthService } from '../service/auth.service';

export class ProfileController {
  constructor(private readonly authService: AuthService) {}

  me = async (request: Request, response: Response): Promise<void> => {
    const authHeader = request.header('authorization') ?? '';
    const token = authHeader.startsWith('Bearer ') ? authHeader.slice(7) : '';
    if (!token) {
      response.status(401).json({ message: 'Missing bearer token.' });
      return;
    }

    const user = this.authService.authenticate(token);
    response.json(await this.authService.getProfile(user.username));
  };
}
