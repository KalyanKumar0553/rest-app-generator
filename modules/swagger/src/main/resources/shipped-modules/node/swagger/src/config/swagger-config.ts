type ModuleManifest = {
  generator: string;
  selectedModules: string[];
  moduleConfigs?: Record<string, Record<string, unknown>>;
};

export type SwaggerModuleConfig = {
  title?: string;
  description?: string;
  version?: string;
  docsPath?: string;
  openApiPath?: string;
  enableUi?: boolean;
};

const normalizePath = (value: unknown, fallback: string): string => {
  const candidate = typeof value === 'string' && value.trim() ? value.trim() : fallback;
  return candidate.startsWith('/') ? candidate : `/${candidate}`;
};

export const resolveSwaggerConfig = (
  config: Record<string, unknown>,
  manifest: ModuleManifest
): Required<SwaggerModuleConfig> => ({
  title: typeof config.title === 'string' && config.title.trim()
    ? config.title.trim()
    : 'Generated API',
  description: typeof config.description === 'string' && config.description.trim()
    ? config.description.trim()
    : `OpenAPI surface for generated modules: ${manifest.selectedModules.join(', ') || 'none'}`,
  version: typeof config.version === 'string' && config.version.trim()
    ? config.version.trim()
    : '1.0.0',
  docsPath: normalizePath(config.docsPath, '/swagger'),
  openApiPath: normalizePath(config.openApiPath, '/api-docs'),
  enableUi: typeof config.enableUi === 'boolean' ? config.enableUi : true
});
