import type { AuthProviderStatusDto } from '../dto/auth.dto';
import type { AuthModuleConfig } from '../config/auth-config';

export class OAuthProviderService {
  constructor(private readonly config: Required<AuthModuleConfig>) {}

  listProviders(): AuthProviderStatusDto[] {
    return this.config.oauthProviders.map((provider) => ({
      provider: provider.provider,
      enabled: provider.enabled,
      clientId: provider.clientId
    }));
  }
}
