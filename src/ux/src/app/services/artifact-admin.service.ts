import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG, API_ENDPOINTS } from '../constants/api.constants';

export interface ArtifactAppPayload {
  code: string;
  name: string;
  description?: string | null;
  status: string;
  generatorLanguage: string;
  buildTool: string;
  enabledPacks: string[];
  config: Record<string, any>;
}

export interface ArtifactAppVersion {
  id: string;
  versionCode: string;
  published: boolean;
  createdByUserId: string;
  config: Record<string, any>;
  createdAt: string;
  updatedAt: string;
}

export interface ArtifactApp {
  id: string;
  code: string;
  name: string;
  description?: string | null;
  status: string;
  ownerUserId: string;
  generatorLanguage: string;
  buildTool: string;
  enabledPacks: string[];
  config: Record<string, any>;
  publishedVersion?: string | null;
  versionCount: number;
  createdAt: string;
  updatedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class ArtifactAdminService {
  constructor(private http: HttpClient) {}

  listApps(): Observable<ArtifactApp[]> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.ADMIN.ARTIFACT_APPS}`;
    return this.http.get<ArtifactApp[]>(url);
  }

  getApp(appId: string): Observable<ArtifactApp> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.ADMIN.ARTIFACT_APP(appId)}`;
    return this.http.get<ArtifactApp>(url);
  }

  createApp(payload: ArtifactAppPayload): Observable<ArtifactApp> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.ADMIN.ARTIFACT_APPS}`;
    return this.http.post<ArtifactApp>(url, payload);
  }

  updateApp(appId: string, payload: ArtifactAppPayload): Observable<ArtifactApp> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.ADMIN.ARTIFACT_APP(appId)}`;
    return this.http.put<ArtifactApp>(url, payload);
  }

  listVersions(appId: string): Observable<ArtifactAppVersion[]> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.ADMIN.ARTIFACT_APP_VERSIONS(appId)}`;
    return this.http.get<ArtifactAppVersion[]>(url);
  }

  createVersion(appId: string, versionCode?: string | null): Observable<ArtifactAppVersion> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.ADMIN.ARTIFACT_APP_VERSIONS(appId)}`;
    return this.http.post<ArtifactAppVersion>(url, versionCode ? { versionCode } : {});
  }

  publishApp(appId: string, versionCode?: string | null): Observable<ArtifactApp> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.ADMIN.ARTIFACT_APP_PUBLISH(appId)}`;
    return this.http.post<ArtifactApp>(url, versionCode ? { versionCode } : {});
  }
}
