import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { API_CONFIG, API_ENDPOINTS } from '../constants/api.constants';
import { MockApiService } from './mock-api.service';

export interface Project {
  id: string;
  name: string;
  description: string;
  createdAt: string;
  updatedAt: string;
  status: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  constructor(private mockApiService: MockApiService) {}

  getProjects(): Observable<Project[]> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.LIST}`;
    return this.mockApiService.get<any>(url, '/assets/mock/projects-response.json').pipe(
      map((response: any) => response?.data?.projects || response?.projects || [])
    );
  }

  getProjectDetails(projectId: string): Observable<Project> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PROJECT.GET(projectId)}`;
    return this.mockApiService.get<any>(url, '/assets/mock/projects-response.json').pipe(
      map((response: any) => {
        const project =
          response?.data?.project ||
          response?.project ||
          (response?.data?.projects || response?.projects || []).find((p: Project) => p.id === projectId);
        return project || response;
      })
    );
  }
}
