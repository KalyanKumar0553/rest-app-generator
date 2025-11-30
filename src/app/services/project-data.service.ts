import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface ProjectSettings {
  projectGroup: string;
  projectName: string;
  buildType: 'gradle' | 'maven';
  language: 'java' | 'kotlin';
  frontend: string;
}

export interface DatabaseSettings {
  database: string;
  dbGeneration: string;
  pluralizeTableNames: boolean;
  addDateCreatedLastUpdated: boolean;
}

export interface DeveloperPreferences {
  applFormat: 'yaml' | 'properties';
  packages: 'technical' | 'domain' | 'mixed';
  enableOpenAPI: boolean;
  useDockerCompose: boolean;
  javaVersion: string;
  deployment: string;
}

export interface Field {
  type: string;
  name: string;
  maxLength?: number;
  primaryKey?: boolean;
  required?: boolean;
  unique?: boolean;
}

export interface Entity {
  name: string;
  mappedSuperclass: boolean;
  addRestEndpoints: boolean;
  fields: Field[];
}

export interface Relation {
  sourceEntity: string;
  targetEntity: string;
  relationType: string;
  fieldName: string;
}

export interface ProjectData {
  id?: number;
  settings: ProjectSettings;
  database: DatabaseSettings;
  preferences: DeveloperPreferences;
  dependencies: string[];
  entities: Entity[];
  relations: Relation[];
}

@Injectable({
  providedIn: 'root'
})
export class ProjectDataService {
  private projectDataSubject = new BehaviorSubject<ProjectData | null>(null);
  public projectData$: Observable<ProjectData | null> = this.projectDataSubject.asObservable();

  constructor() {}

  getProjectData(): ProjectData | null {
    return this.projectDataSubject.value;
  }

  setProjectData(data: ProjectData): void {
    this.projectDataSubject.next(data);
  }

  updateSettings(settings: ProjectSettings): void {
    const current = this.projectDataSubject.value;
    if (current) {
      this.projectDataSubject.next({ ...current, settings });
    }
  }

  updateDatabase(database: DatabaseSettings): void {
    const current = this.projectDataSubject.value;
    if (current) {
      this.projectDataSubject.next({ ...current, database });
    }
  }

  updatePreferences(preferences: DeveloperPreferences): void {
    const current = this.projectDataSubject.value;
    if (current) {
      this.projectDataSubject.next({ ...current, preferences });
    }
  }

  updateDependencies(dependencies: string[]): void {
    const current = this.projectDataSubject.value;
    if (current) {
      this.projectDataSubject.next({ ...current, dependencies });
    }
  }

  addEntity(entity: Entity): void {
    const current = this.projectDataSubject.value;
    if (current) {
      const entities = [...current.entities, entity];
      this.projectDataSubject.next({ ...current, entities });
    }
  }

  updateEntity(index: number, entity: Entity): void {
    const current = this.projectDataSubject.value;
    if (current && current.entities[index]) {
      const entities = [...current.entities];
      entities[index] = entity;
      this.projectDataSubject.next({ ...current, entities });
    }
  }

  deleteEntity(index: number): void {
    const current = this.projectDataSubject.value;
    if (current) {
      const entities = current.entities.filter((_, i) => i !== index);
      this.projectDataSubject.next({ ...current, entities });
    }
  }

  addRelation(relation: Relation): void {
    const current = this.projectDataSubject.value;
    if (current) {
      const relations = [...current.relations, relation];
      this.projectDataSubject.next({ ...current, relations });
    }
  }

  deleteRelation(index: number): void {
    const current = this.projectDataSubject.value;
    if (current) {
      const relations = current.relations.filter((_, i) => i !== index);
      this.projectDataSubject.next({ ...current, relations });
    }
  }

  exportToJSON(): string {
    const data = this.projectDataSubject.value;
    return JSON.stringify(data, null, 2);
  }

  importFromJSON(json: string): boolean {
    try {
      const data = JSON.parse(json);
      this.projectDataSubject.next(data);
      return true;
    } catch (error) {
      console.error('Failed to parse JSON:', error);
      return false;
    }
  }

  clearProjectData(): void {
    this.projectDataSubject.next(null);
  }
}
