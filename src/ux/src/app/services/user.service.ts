import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { API_CONFIG, API_ENDPOINTS } from '../constants/api.constants';
import { MockApiService } from './mock-api.service';
import { AuthService } from './auth.service';

export interface UserRoles {
  userId: string;
  roles: string[];
  permissions: string[];
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  constructor(
    private http: HttpClient,
    private mockApiService: MockApiService,
    private authService: AuthService
  ) {}

  getUserRoles(): Observable<UserRoles> {
    const url = `${API_CONFIG.AUTH_BASE_URL}${API_ENDPOINTS.AUTH.ROLES}`;
    return this.mockApiService.get<any>(url, '/assets/mock/user-roles-response.json').pipe(
      map((response: any) => {
        const data = response.data || response;
        return {
          userId: this.authService.currentUserValue?.id || '',
          roles: data.roles || [],
          permissions: data.permissions || []
        };
      })
    );
  }

  getUserProfile(): Observable<any> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.USER.PROFILE}`;
    return this.http.get(url);
  }

  updateUserProfile(data: any): Observable<any> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.USER.UPDATE_PROFILE}`;
    return this.http.put(url, data);
  }
}
