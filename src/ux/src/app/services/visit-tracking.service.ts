import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_CONFIG, API_ENDPOINTS } from '../constants/api.constants';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class VisitTrackingService {
  constructor(private http: HttpClient) {}

  trackHomeVisit(): Observable<void> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.ANALYTICS.TRACK_HOME_VISIT}`;
    return this.http.post<void>(url, {});
  }
}
