import type { Express, Request, Response } from 'express';
import swaggerUi from 'swagger-ui-express';

import { resolveSwaggerConfig } from './config/swagger-config';

type ModuleManifest = {
  generator: string;
  selectedModules: string[];
  moduleConfigs?: Record<string, Record<string, unknown>>;
};

const buildOpenApiDocument = (
  config: ReturnType<typeof resolveSwaggerConfig>,
  manifest: ModuleManifest
): Record<string, unknown> => {
  const tags = manifest.selectedModules.map((moduleId) => ({
    name: moduleId,
    description: `Generated module ${moduleId}`
  }));
  const paths = manifest.selectedModules.reduce<Record<string, unknown>>((accumulator, moduleId) => {
    accumulator[`/api/modules/${moduleId}`] = {
      get: {
        tags: [moduleId],
        summary: `Inspect ${moduleId} module`,
        responses: {
          '200': {
            description: 'Module response'
          }
        }
      }
    };
    return accumulator;
  }, {});

  return {
    openapi: '3.0.3',
    info: {
      title: config.title,
      description: config.description,
      version: config.version
    },
    tags,
    paths
  };
};

export const registerModule = (
  app: Express,
  config: Record<string, unknown>,
  manifest: ModuleManifest
): void => {
  const resolvedConfig = resolveSwaggerConfig(config, manifest);
  const document = buildOpenApiDocument(resolvedConfig, manifest);

  app.get(resolvedConfig.openApiPath, (_request: Request, response: Response) => {
    response.json(document);
  });

  app.get('/api/modules/swagger/config', (_request: Request, response: Response) => {
    response.json({
      title: resolvedConfig.title,
      description: resolvedConfig.description,
      version: resolvedConfig.version,
      docsPath: resolvedConfig.docsPath,
      openApiPath: resolvedConfig.openApiPath,
      enableUi: resolvedConfig.enableUi
    });
  });

  if (resolvedConfig.enableUi) {
    app.use(resolvedConfig.docsPath, swaggerUi.serve, swaggerUi.setup(document, {
      explorer: true
    }));
  }
};
