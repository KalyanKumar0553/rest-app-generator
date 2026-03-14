export function resolveProjectGenerationRoute(language: string | null | undefined): string {
  const normalized = String(language ?? '').trim().toLowerCase();

  const routesByLanguage: Record<string, string> = {
    java: '/project-generation',
    kotlin: '/project-generation',
    node: '/project-generation-node'
  };

  return routesByLanguage[normalized] ?? '/project-generation';
}
