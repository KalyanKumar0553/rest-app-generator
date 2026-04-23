import express, { type Express } from 'express';

import { resolveCdnConfig } from './config/cdn-config';
import { CdnController } from './controller/cdn.controller';
import { CdnManifestRepository } from './repository/cdn-manifest.repository';
import { buildCdnRouter } from './route/cdn.routes';
import { createCdnStorageService } from './service/cdn-storage.service';

type ModuleManifest = {
  generator: string;
  selectedModules: string[];
  moduleConfigs?: Record<string, Record<string, unknown>>;
};

export const registerModule = (
  app: Express,
  config: Record<string, unknown>,
  _manifest: ModuleManifest
): void => {
  const resolvedConfig = resolveCdnConfig(config);
  const manifestRepository = new CdnManifestRepository(resolvedConfig.localDirectory);
  const storageService = createCdnStorageService(resolvedConfig);
  const controller = new CdnController(resolvedConfig, storageService, manifestRepository);

  if (resolvedConfig.provider === 'local' && resolvedConfig.publicBaseUrl.startsWith('/')) {
    app.use(resolvedConfig.publicBaseUrl, express.static(resolvedConfig.localDirectory));
  }

  app.use('/api/modules/cdn', buildCdnRouter(controller, resolvedConfig));
};
