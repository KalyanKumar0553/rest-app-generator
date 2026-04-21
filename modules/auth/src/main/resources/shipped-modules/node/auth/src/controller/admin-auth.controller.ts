import type { Request, Response } from 'express';

import { OAuthProviderService } from '../service/oauth-provider.service';
import { UserRepository } from '../repository/user.repository';

export class AdminAuthController {
  constructor(
    private readonly userRepository: UserRepository,
    private readonly providerService: OAuthProviderService
  ) {}

  users = async (_request: Request, response: Response): Promise<void> => {
    const users = await this.userRepository.findAll();
    response.json(users.map((user) => ({
      id: user.id,
      username: user.username,
      email: user.email,
      status: user.status,
      timezone: user.timezone ?? 'UTC'
    })));
  };

  providers = (_request: Request, response: Response): void => {
    response.json(this.providerService.listProviders());
  };
}
