import type { Request, Response } from 'express';

import type { AuthModuleConfig } from '../config/auth-config';
import { AuthService } from '../service/auth.service';
import { OAuthProviderService } from '../service/oauth-provider.service';

export class AuthController {
  constructor(
    private readonly authService: AuthService,
    private readonly providerService: OAuthProviderService,
    private readonly config: Required<AuthModuleConfig>
  ) {}

  status = (_request: Request, response: Response): void => {
    response.json({
      module: 'auth',
      publicEndpoints: this.config.publicEndpoints,
      oauthProviders: this.providerService.listProviders()
    });
  };

  login = async (request: Request, response: Response): Promise<void> => {
    const username = String(request.body?.username ?? '').trim();
    const password = String(request.body?.password ?? '');
    if (!username || !password) {
      response.status(400).json({ message: 'Username and password are required.' });
      return;
    }

    const tokens = await this.authService.login({ username, password });
    response.json(tokens);
  };

  register = async (request: Request, response: Response): Promise<void> => {
    const username = String(request.body?.username ?? '').trim();
    const email = String(request.body?.email ?? '').trim();
    const password = String(request.body?.password ?? '');
    if (!username || !email || !password) {
      response.status(400).json({ message: 'username, email and password are required.' });
      return;
    }

    const profile = await this.authService.register({ username, email, password });
    response.status(201).json(profile);
  };

  refresh = async (request: Request, response: Response): Promise<void> => {
    const refreshToken = String(request.body?.refreshToken ?? '').trim();
    if (!refreshToken) {
      response.status(400).json({ message: 'Refresh token is required.' });
      return;
    }
    response.json(await this.authService.refresh(refreshToken));
  };

  providers = (_request: Request, response: Response): void => {
    response.json(this.providerService.listProviders());
  };
}
