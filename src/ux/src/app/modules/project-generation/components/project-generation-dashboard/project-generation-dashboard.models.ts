export interface ProjectSettings {
  projectGroup: string;
  projectName: string;
  buildType: 'gradle' | 'maven';
  language: 'java' | 'kotlin';
  frontend: string;
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
  id: string;
  projectId: string;
  status: string;
  createdAt?: string;
  runNumber?: number;
}

export interface ControllerRestSpecRow {
  key: string;
  name: string;
  totalEndpoints: number;
  mappedEntities: string[];
  entityIndexes: number[];
  hasControllersConfig: boolean;
}
