import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG, API_ENDPOINTS } from '../constants/api.constants';

export interface ConfigPropertyValue {
  valueKey: string;
  valueLabel: string;
}

export interface ConfigProperty {
  category: string;
  label: string;
  propertyKey: string;
  currentValueKey?: string | null;
  values: ConfigPropertyValue[];
}

export interface ConfigFeatureValuePayload {
  category: string;
  propertyKey: string;
  valueKey: string;
}

export interface PluginModuleVersion {
  id: string;
  versionCode: string;
  changelog?: string | null;
  fileName: string;
  sizeBytes: number;
  published: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface PluginModule {
  id: string;
  code: string;
  name: string;
  description?: string | null;
  category?: string | null;
  enabled: boolean;
  enableConfig: boolean;
  generatorTargets: string[];
  currentPublishedVersionId?: string | null;
  createdAt?: string;
  updatedAt?: string;
  versions: PluginModuleVersion[];
}

export interface PluginModulePayload {
  code: string;
  name: string;
  description?: string | null;
  category?: string | null;
  enabled: boolean;
  enableConfig: boolean;
  generatorTargets: string[];
}

export interface PluginModuleVersionPayload {
  versionCode: string;
  changelog?: string | null;
}

export interface ProjectTabDefinitionAdmin {
  id: string;
  key: string;
  label: string;
  icon: string;
  componentKey: string;
  order: number;
  generatorLanguage: string;
  enabled: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProjectTabDefinitionPayload {
  key: string;
  label: string;
  icon: string;
  componentKey: string;
  order: number;
  generatorLanguage: string;
  enabled: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  constructor(private http: HttpClient) {}

  getConfigFeatures(): Observable<ConfigProperty[]> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.ADMIN.CONFIG_FEATURES}`;
    return this.http.get<ConfigProperty[]>(url);
  }

  updateConfigFeatureValue(payload: ConfigFeatureValuePayload): Observable<ConfigProperty> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.ADMIN.UPDATE_CONFIG_FEATURE_VALUE}`;
    return this.http.put<ConfigProperty>(url, payload);
  }

  getPluginModules(): Observable<PluginModule[]> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.ADMIN.PLUGIN_MODULES}`;
    return this.http.get<PluginModule[]>(url);
  }

  createPluginModule(payload: PluginModulePayload, version: PluginModuleVersionPayload, artifact: File): Observable<PluginModule> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.ADMIN.PLUGIN_MODULES}`;
    const formData = this.buildPluginModuleFormData(payload, version, artifact);
    return this.http.post<PluginModule>(url, formData);
  }

  updatePluginModule(moduleId: string, payload: PluginModulePayload): Observable<PluginModule> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.ADMIN.UPDATE_PLUGIN_MODULE(moduleId)}`;
    const formData = new FormData();
    formData.append('code', payload.code);
    formData.append('name', payload.name);
    formData.append('description', payload.description ?? '');
    formData.append('category', payload.category ?? '');
    formData.append('enabled', String(payload.enabled));
    formData.append('enableConfig', String(payload.enableConfig));
    payload.generatorTargets.forEach((target) => formData.append('generatorTargets', target));
    return this.http.put<PluginModule>(url, formData);
  }

  uploadPluginModuleVersion(moduleId: string, version: PluginModuleVersionPayload, artifact: File): Observable<PluginModule> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.ADMIN.PLUGIN_MODULE_VERSIONS(moduleId)}`;
    const formData = new FormData();
    formData.append('versionCode', version.versionCode);
    formData.append('changelog', version.changelog ?? '');
    formData.append('artifact', artifact);
    return this.http.post<PluginModule>(url, formData);
  }

  publishPluginModuleVersion(moduleId: string, versionId: string): Observable<PluginModule> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.ADMIN.PUBLISH_PLUGIN_MODULE_VERSION(moduleId, versionId)}`;
    return this.http.post<PluginModule>(url, {});
  }

  getProjectTabDefinitions(): Observable<ProjectTabDefinitionAdmin[]> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.ADMIN.PROJECT_TAB_DEFINITIONS}`;
    return this.http.get<ProjectTabDefinitionAdmin[]>(url);
  }

  createProjectTabDefinition(payload: ProjectTabDefinitionPayload): Observable<ProjectTabDefinitionAdmin> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.ADMIN.PROJECT_TAB_DEFINITIONS}`;
    return this.http.post<ProjectTabDefinitionAdmin>(url, payload);
  }

  updateProjectTabDefinition(tabId: string, payload: ProjectTabDefinitionPayload): Observable<ProjectTabDefinitionAdmin> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.ADMIN.UPDATE_PROJECT_TAB_DEFINITION(tabId)}`;
    return this.http.put<ProjectTabDefinitionAdmin>(url, payload);
  }

  deleteProjectTabDefinition(tabId: string): Observable<void> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.ADMIN.UPDATE_PROJECT_TAB_DEFINITION(tabId)}`;
    return this.http.delete<void>(url);
  }

  private buildPluginModuleFormData(payload: PluginModulePayload, version: PluginModuleVersionPayload, artifact: File): FormData {
    const formData = new FormData();
    formData.append('code', payload.code);
    formData.append('name', payload.name);
    formData.append('description', payload.description ?? '');
    formData.append('category', payload.category ?? '');
    formData.append('enabled', String(payload.enabled));
    formData.append('enableConfig', String(payload.enableConfig));
    payload.generatorTargets.forEach((target) => formData.append('generatorTargets', target));
    formData.append('versionCode', version.versionCode);
    formData.append('changelog', version.changelog ?? '');
    formData.append('artifact', artifact);
    return formData;
  }
}
