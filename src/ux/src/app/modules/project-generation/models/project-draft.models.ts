export interface ProjectControllersDraft {
  enabled: boolean;
  config: any;
}

export interface ProjectActuatorDraft {
  selectedConfiguration: string;
  configurations: Record<string, string[]>;
}

export interface ProjectDraftState<TProjectSettings, TDatabaseSettings, TDeveloperPreferences> {
  id?: string;
  settings: TProjectSettings;
  database: TDatabaseSettings;
  preferences: TDeveloperPreferences;
  controllers: ProjectControllersDraft;
  actuator?: ProjectActuatorDraft;
  dependencies: string;
  selectedDependencies: string[];
  entities: any[];
  dataObjects: any[];
  relations: any[];
  enums: any[];
  mappers: any[];
  moduleConfigs?: Record<string, any>;
  selectedPlugins?: Array<Record<string, any>>;
}
