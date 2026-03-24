export function resolveProjectGenerationRoute(language: string | null | undefined): string {
  const normalized = String(language ?? '').trim().toLowerCase();

  const routesByLanguage: Record<string, string> = {
    java: '/project-generation',
    kotlin: '/project-generation',
    node: '/project-generation-node',
    python: '/project-generation-python',
    py: '/project-generation-python'
  };

  return routesByLanguage[normalized] ?? '/project-generation';
}
