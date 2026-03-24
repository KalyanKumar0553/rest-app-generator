import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG, API_ENDPOINTS } from '../constants/api.constants';

export interface AiLabsStep {
  key: string;
  label: string;
  status: string;
  message?: string;
  updatedAt?: string;
}

export interface AiLabsJobStatus {
  jobId: string;
  status: string;
  prompt: string;
  steps: AiLabsStep[];
  streamPreview?: string;
  projectId?: string;
  generator?: string;
  errorMessage?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface AiLabsGenerateResponse {
  jobId: string;
  status: string;
}

@Injectable({
  providedIn: 'root'
})
export class AiLabsService {
  constructor(private http: HttpClient) {}

  generateProject(prompt: string): Observable<AiLabsGenerateResponse> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.AI_LABS.JOBS}`;
    return this.http.post<AiLabsGenerateResponse>(url, { prompt });
  }

  getJob(jobId: string): Observable<AiLabsJobStatus> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.AI_LABS.JOB(jobId)}`;
    return this.http.get<AiLabsJobStatus>(url);
  }

  getJobEventsUrl(jobId: string): string {
    return `${API_CONFIG.BASE_URL}${API_ENDPOINTS.AI_LABS.JOB_EVENTS(jobId)}`;
  }
}
