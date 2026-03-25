import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { API_CONFIG, API_ENDPOINTS } from '../constants/api.constants';
import { AuthService } from './auth.service';

export interface UserRoles {
  userId: string;
  roles: string[];
  permissions: string[];
}

export interface UserProfile {
  userId: string;
  email: string;
  name: string;
  firstName?: string | null;
  lastName?: string | null;
  avatarUrl?: string | null;
  timeZoneId?: string | null;
}

export interface UpdateUserProfilePayload {
  timeZoneId?: string | null;
}

export interface ChangePasswordPayload {
  currentPassword: string;
  newPassword: string;
}

export interface UserSearchResult {
  userId: string;
  name: string;
  email: string;
  avatarUrl?: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  getUserRoles(): Observable<UserRoles> {
    const url = `${API_CONFIG.AUTH_BASE_URL}${API_ENDPOINTS.AUTH.ROLES}`;
    return this.http.get<any>(url).pipe(
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

  getUserProfile(): Observable<UserProfile> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.USER.PROFILE}`;
    return this.http.get<any>(url).pipe(map((response: any) => response.data || response));
  }

  updateUserProfile(data: UpdateUserProfilePayload): Observable<UserProfile> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.USER.UPDATE_PROFILE}`;
    return this.http.put<any>(url, data).pipe(map((response: any) => response.data || response));
  }

  changePassword(payload: ChangePasswordPayload): Observable<void> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.USER.CHANGE_PASSWORD}`;
    return this.http.post<any>(url, payload).pipe(map(() => void 0));
  }

  searchUsers(query: string): Observable<UserSearchResult[]> {
    const url = `${API_CONFIG.BASE_URL}${API_ENDPOINTS.USER.SEARCH}?query=${encodeURIComponent(query)}`;
    return this.http.get<any>(url).pipe(map((response: any) => response.data || response));
  }
}
