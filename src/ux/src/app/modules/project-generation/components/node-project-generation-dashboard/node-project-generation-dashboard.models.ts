export interface ProjectSettings {
  projectGroup: string;
  projectName: string;
  projectDescription: string;
  buildType: 'gradle' | 'maven';
  language: 'java' | 'kotlin' | 'node' | 'python';
  frontend: string;
  packageManager: 'npm' | 'pnpm' | 'yarn';
  serverPort: number;
}

export interface DatabaseSettings {
  dbType: 'SQL' | 'NOSQL' | 'NONE';
  database: string;
  dbGeneration: string;
  pluralizeTableNames: boolean;
}

export interface DatabaseOption {
  value: string;
  label: string;
  type: 'SQL' | 'NOSQL';
}

export interface DeveloperPreferences {
  applFormat: 'yaml' | 'properties';
  packages: 'technical' | 'domain' | 'mixed';
  enableOpenAPI: boolean;
  enableActuator: boolean;
  configureApi: boolean;
  enableLombok: boolean;
  useDockerCompose: boolean;
  profiles: string[];
  javaVersion: string;
  deployment: string;
}

export interface ProjectRunSummary {
  id?: string;
  runId?: string;
  projectId: string;
  ownerId?: string;
  type?: string;
  status: string;
  hasZip?: boolean;
  errorMessage?: string;
  createdAt?: string;
  updatedAt?: string;
  runNumber?: number;
}

export interface ProjectGenerationStageEvent {
  stage: string;
  stepName?: string;
  stepOrder?: number;
  totalSteps?: number;
  attempt?: number;
  retryEnabled?: boolean;
  runId?: string;
  status: string;
  message?: string;
  timestamp?: string;
}

export interface ControllerRestSpecRow {
  key: string;
  name: string;
  totalEndpoints: number;
  mappedEntities: string[];
  entityIndexes: number[];
  hasControllersConfig: boolean;
}
