export type SupportedProjectLanguage = 'java' | 'kotlin' | 'node' | 'python';

export interface ProjectLanguageOption {
  value: SupportedProjectLanguage;
  label: string;
  summary: string;
  runtime: string;
}

const LANGUAGE_OPTIONS: ProjectLanguageOption[] = [
  {
    value: 'java',
    label: 'Java',
    summary: 'Spring Boot generation with the standard Java source layout and build pipeline.',
    runtime: 'JVM'
  },
  {
    value: 'kotlin',
    label: 'Kotlin',
    summary: 'Spring Boot generation with Kotlin-friendly scaffolding and Kotlin source output.',
    runtime: 'JVM'
  },
  {
    value: 'node',
    label: 'Node.js',
    summary: 'JavaScript server generation with the Node runtime and Express-oriented project shape.',
    runtime: 'Node'
  },
  {
    value: 'python',
    label: 'Python',
    summary: 'Python server generation with FastAPI-oriented output and Python packaging defaults.',
    runtime: 'Python'
  }
];

export function normalizeProjectLanguage(value: string | null | undefined): SupportedProjectLanguage {
  const normalized = String(value ?? '').trim().toLowerCase();
  switch (normalized) {
    case 'kotlin':
    case 'kt':
      return 'kotlin';
    case 'node':
    case 'nodejs':
    case 'javascript':
    case 'js':
      return 'node';
    case 'python':
    case 'py':
      return 'python';
    default:
      return 'java';
  }
}

export function getProjectLanguageLabel(value: string | null | undefined): string {
  const normalized = normalizeProjectLanguage(value);
  return LANGUAGE_OPTIONS.find((option) => option.value === normalized)?.label ?? 'Java';
}

export function getProjectLanguageOption(value: string | null | undefined): ProjectLanguageOption {
  const normalized = normalizeProjectLanguage(value);
  return LANGUAGE_OPTIONS.find((option) => option.value === normalized) ?? LANGUAGE_OPTIONS[0];
}

export function getMigrationTargetLanguageOptions(currentLanguage: string | null | undefined): ProjectLanguageOption[] {
  const normalizedCurrent = normalizeProjectLanguage(currentLanguage);
  return LANGUAGE_OPTIONS.filter((option) => option.value !== normalizedCurrent && option.value !== 'kotlin');
}
