import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_CONFIG, API_ENDPOINTS } from '../constants/api.constants';

export interface NewsletterSubscribeResponse {
  subscribed: boolean;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class NewsletterService {
  constructor(private http: HttpClient) {}

  subscribe(email: string): Observable<NewsletterSubscribeResponse> {
    const payload = { email };
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.NEWSLETTER.SUBSCRIBE}`;
    return this.http.post<NewsletterSubscribeResponse>(url, payload);
  }
}
