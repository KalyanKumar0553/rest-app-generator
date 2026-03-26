import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG, API_ENDPOINTS, STORAGE_KEYS } from '../constants/api.constants';
import { LocalStorageService } from './local-storage.service';

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

export interface AiLabsJobSummary {
  jobId: string;
  generatedBy: string;
  status: string;
  generator?: string;
  projectId?: string;
  generatedOn?: string;
  updatedAt?: string;
}

export interface AiLabsGenerateResponse {
  jobId: string;
  status: string;
}

export interface AiLabsAvailability {
  enabled: boolean;
  usageLimit?: number | null;
  usedCount: number;
  remainingCount?: number | null;
  limitReached: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AiLabsService {
  constructor(
    private http: HttpClient,
    private localStorageService: LocalStorageService
  ) {}

  generateProject(prompt: string): Observable<AiLabsGenerateResponse> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.AI_LABS.JOBS}`;
    return this.http.post<AiLabsGenerateResponse>(url, { prompt });
  }

  getAvailability(): Observable<AiLabsAvailability> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.AI_LABS.AVAILABILITY}`;
    return this.http.get<AiLabsAvailability>(url);
  }

  getJob(jobId: string): Observable<AiLabsJobStatus> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.AI_LABS.JOB(jobId)}`;
    return this.http.get<AiLabsJobStatus>(url);
  }

  listJobs(): Observable<AiLabsJobSummary[]> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.AI_LABS.JOBS}`;
    return this.http.get<AiLabsJobSummary[]>(url);
  }

  getJobEventsUrl(jobId: string): string {
    return this.appendAccessToken(`${API_CONFIG.BASE_URL}${API_ENDPOINTS.AI_LABS.JOB_EVENTS(jobId)}`);
  }

  private appendAccessToken(url: string): string {
    const token = this.localStorageService.getItem(STORAGE_KEYS.ACCESS_TOKEN);
    if (!token) {
      return url;
    }
    const separator = url.includes('?') ? '&' : '?';
    return `${url}${separator}access_token=${encodeURIComponent(token)}`;
  }
}
