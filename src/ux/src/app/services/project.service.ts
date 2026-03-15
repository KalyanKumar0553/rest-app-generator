import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG, API_ENDPOINTS } from '../constants/api.constants';

export interface ProjectSummary {
  projectId: string;
  artifact?: string;
  id?: string;
  name?: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProjectContributor {
  id?: string;
  userId: string;
  createdAt?: string;
}

export interface ProjectDetails extends ProjectSummary {
  yaml?: string;
  ownerId?: string;
  contributorAccess?: boolean;
  canManageContributors?: boolean;
  contributors?: ProjectContributor[];
}

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  constructor(private http: HttpClient) {}

  getProjects(): Observable<ProjectSummary[]> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.LIST}`;
    return this.http.get<ProjectSummary[]>(url);
  }

  getProject(projectId: string): Observable<ProjectDetails> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.GET(projectId)}`;
    return this.http.get<ProjectDetails>(url);
  }

  getProjectContributors(projectId: string): Observable<ProjectContributor[]> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.GET(projectId)}/contributors`;
    return this.http.get<ProjectContributor[]>(url);
  }

  addProjectContributor(projectId: string, userId: string): Observable<ProjectContributor[]> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.GET(projectId)}/contributors`;
    return this.http.post<ProjectContributor[]>(url, { userId });
  }

  removeProjectContributor(projectId: string, userId: string): Observable<void> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.GET(projectId)}/contributors?userId=${encodeURIComponent(userId)}`;
    return this.http.delete<void>(url);
  }
}
