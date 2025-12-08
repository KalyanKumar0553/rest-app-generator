import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { API_CONFIG, API_ENDPOINTS } from '../constants/api.constants';
import { MockApiService } from './mock-api.service';

export interface DependencyDTO {
  id: string;
}

@Injectable({
  providedIn: 'root'
})
export class DependencyService {
  constructor(private mockApiService: MockApiService) {}

  getDependencies(): Observable<DependencyDTO[]> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.CONFIG.DEPENDENCIES}`;
    return this.mockApiService.get<any>(url, '/assets/mock/dependencies-response.json').pipe(
      map((response: any) => response?.data || response?.dependencies || response || [])
    );
  }
}
