import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { tap, catchError, map, finalize, shareReplay } from 'rxjs/operators';
import { Router } from '@angular/router';
import { API_CONFIG, API_ENDPOINTS, STORAGE_KEYS } from '../constants/api.constants';
import { LocalStorageService } from './local-storage.service';
import { MockApiService } from './mock-api.service';

export interface CaptchaChallenge {
  captchaId: string;
  imageBase64: string;
}

export interface SignupRequest {
  identifier: string;
  password: string;
  captchaId: string;
  captchaText: string;
  acceptTerms?: boolean;
  receiveUpdates?: boolean;
}

export interface LoginRequest {
  identifier: string;
  password: string;
}

export interface SendOTPRequest {
  identifier: string;
  captchaId: string;
  captchaText: string;
}

export interface VerifyOTPRequest {
  identifier: string;
  otp: string;
}

export interface ResetPasswordWithOTPRequest {
  identifier: string;
  otp: string;
  newPassword: string;
}

export interface ForgotPasswordRequest {
  identifier: string;
  captchaId: string;
  captchaText: string;
}

export interface AuthResponse {
  accessToken?: string;
  refreshToken?: string;
  user?: UserData;
  message?: string;
}

export interface UserData {
  id?: string;
  email: string;
  name?: string;
  role?: string;
}

export interface RefreshTokenResponse {
  accessToken: string;
  refreshToken?: string;
}

export interface AuthProvidersResponse {
  googleEnabled: boolean;
  keycloakEnabled: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject: BehaviorSubject<UserData | null>;
  public currentUser$: Observable<UserData | null>;
  private isAuthenticatedSubject: BehaviorSubject<boolean>;
  public isAuthenticated$: Observable<boolean>;
  private refreshInFlight$: Observable<RefreshTokenResponse> | null = null;

  constructor(
    private http: HttpClient,
    private localStorageService: LocalStorageService,
    private router: Router,
    private mockApiService: MockApiService
  ) {
    const userData = this.localStorageService.getItem(STORAGE_KEYS.USER_DATA);
    this.currentUserSubject = new BehaviorSubject<UserData | null>(
      userData ? JSON.parse(userData) : null
    );
    this.currentUser$ = this.currentUserSubject.asObservable();

    const token = this.localStorageService.getItem(STORAGE_KEYS.ACCESS_TOKEN);
    this.isAuthenticatedSubject = new BehaviorSubject<boolean>(!!token);
    this.isAuthenticated$ = this.isAuthenticatedSubject.asObservable();
  }

  get currentUserValue(): UserData | null {
    return this.currentUserSubject.value;
  }

  get isAuthenticated(): boolean {
    return this.isAuthenticatedSubject.value;
  }

  getCaptcha(): Observable<CaptchaChallenge> {
    const url = `${API_CONFIG.AUTH_BASE_URL}${API_ENDPOINTS.AUTH.CAPTCHA}`;
    return this.mockApiService.get(url, '/assets/mock/captcha-response.json').pipe(
      map((response: any) => response.data || response),
      catchError(error => this.handleAuthError(error))
    );
  }

  signup(data: SignupRequest): Observable<any> {
    const url = `${API_CONFIG.AUTH_BASE_URL}${API_ENDPOINTS.AUTH.SIGNUP}`;
    return this.mockApiService.post(url, data, '/assets/mock/signup-response.json').pipe(
      map((response: any) => response.data || response),
      catchError(error => this.handleAuthError(error))
    );
  }

  login(data: LoginRequest): Observable<AuthResponse> {
    const url = `${API_CONFIG.AUTH_BASE_URL}${API_ENDPOINTS.AUTH.LOGIN}`;
    return this.mockApiService.post<any>(url, data, '/assets/mock/login-response.json').pipe(
      map((response: any) => response.data || response),
      tap(response => this.handleAuthSuccess(response)),
      catchError(error => this.handleAuthError(error))
    );
  }

  logout(): Observable<any> {
    const url = `${API_CONFIG.AUTH_BASE_URL}${API_ENDPOINTS.AUTH.LOGOUT}`;
    const refreshToken = this.getRefreshToken();

    if (!refreshToken) {
      this.handleLogout();
      return throwError(() => new Error('No refresh token available'));
    }

    return this.mockApiService.post(url, { refreshToken }, '/assets/mock/logout-response.json').pipe(
      tap(() => this.handleLogout()),
      catchError(error => {
        this.handleLogout();
        return throwError(() => error);
      })
    );
  }

  sendOTP(data: SendOTPRequest): Observable<any> {
    const url = `${API_CONFIG.AUTH_BASE_URL}${API_ENDPOINTS.AUTH.SEND_OTP}`;
    return this.mockApiService.post(url, data, '/assets/mock/send-otp-response.json').pipe(
      map((response: any) => response.data || response),
      catchError(error => this.handleAuthError(error))
    );
  }

  verifyOTP(data: VerifyOTPRequest): Observable<any> {
    const url = `${API_CONFIG.AUTH_BASE_URL}${API_ENDPOINTS.AUTH.VERIFY_OTP}`;
    return this.mockApiService.post(url, data, '/assets/mock/verify-otp-response.json').pipe(
      map((response: any) => response.data || response),
      tap(response => this.handleAuthSuccess(response)),
      catchError(error => this.handleAuthError(error))
    );
  }

  resetPasswordWithOTP(data: ResetPasswordWithOTPRequest): Observable<any> {
    const url = `${API_CONFIG.AUTH_BASE_URL}${API_ENDPOINTS.AUTH.RESET_PASSWORD_WITH_OTP}`;
    return this.mockApiService.post(url, data, '/assets/mock/reset-password-with-otp-response.json').pipe(
      map((response: any) => response.data || response),
      catchError(error => this.handleAuthError(error))
    );
  }

  forgotPassword(data: ForgotPasswordRequest): Observable<any> {
    const url = `${API_CONFIG.AUTH_BASE_URL}${API_ENDPOINTS.AUTH.FORGOT_PASSWORD}`;
    return this.mockApiService.post(url, data, '/assets/mock/forgot-password-response.json').pipe(
      map((response: any) => response.data || response),
      catchError(error => this.handleAuthError(error))
    );
  }

  refreshToken(): Observable<RefreshTokenResponse> {
    if (this.refreshInFlight$) {
      return this.refreshInFlight$;
    }

    const url = `${API_CONFIG.AUTH_BASE_URL}${API_ENDPOINTS.AUTH.REFRESH_TOKEN}`;
    const refreshToken = this.localStorageService.getItem(STORAGE_KEYS.REFRESH_TOKEN);

    if (!refreshToken) {
      this.handleLogout();
      return throwError(() => new Error('No refresh token available'));
    }

    this.refreshInFlight$ = this.http.post<RefreshTokenResponse>(url, { refreshToken }).pipe(
      tap(response => {
        if (response.accessToken) {
          this.localStorageService.setItem(STORAGE_KEYS.ACCESS_TOKEN, response.accessToken);
          if (response.refreshToken) {
            this.localStorageService.setItem(STORAGE_KEYS.REFRESH_TOKEN, response.refreshToken);
          }
        }
      }),
      catchError(error => {
        this.handleLogout();
        return throwError(() => error);
      }),
      finalize(() => {
        this.refreshInFlight$ = null;
      }),
      shareReplay(1)
    );

    return this.refreshInFlight$;
  }

  getAuthProviders(): Observable<AuthProvidersResponse> {
    const url = `${API_CONFIG.AUTH_BASE_URL}${API_ENDPOINTS.AUTH.PROVIDERS}`;
    return this.http.get<any>(url).pipe(
      map((response: any) => response?.data || response),
      catchError(error => this.handleAuthError(error))
    );
  }

  getAccessToken(): string | null {
    return this.localStorageService.getItem(STORAGE_KEYS.ACCESS_TOKEN);
  }

  getRefreshToken(): string | null {
    return this.localStorageService.getItem(STORAGE_KEYS.REFRESH_TOKEN);
  }

  getAccessTokenExpiration(): number | null {
    const token = this.getAccessToken();
    if (!token) {
      return null;
    }

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return typeof payload?.exp === 'number' ? payload.exp * 1000 : null;
    } catch {
      return null;
    }
  }

  startGoogleLogin(): void {
    if (typeof window === 'undefined') {
      return;
    }
    window.location.href = `${API_CONFIG.AUTH_BASE_URL}${API_ENDPOINTS.AUTH.GOOGLE_OAUTH_START}`;
  }

  startKeycloakLogin(): void {
    if (typeof window === 'undefined') {
      return;
    }
    window.location.href = `${API_CONFIG.AUTH_BASE_URL}${API_ENDPOINTS.AUTH.KEYCLOAK_OAUTH_START}`;
  }

  completeExternalAuth(response: AuthResponse): void {
    this.handleAuthSuccess(response);
  }

  private handleAuthSuccess(response: AuthResponse): void {
    if (response.accessToken) {
      this.localStorageService.setItem(STORAGE_KEYS.ACCESS_TOKEN, response.accessToken);
    }

    if (response.refreshToken) {
      this.localStorageService.setItem(STORAGE_KEYS.REFRESH_TOKEN, response.refreshToken);
    }

    if (response.user) {
      this.localStorageService.setItem(STORAGE_KEYS.USER_DATA, JSON.stringify(response.user));
      this.currentUserSubject.next(response.user);
    }

    this.isAuthenticatedSubject.next(true);
  }

  private handleLogout(): void {
    this.localStorageService.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
    this.localStorageService.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
    this.localStorageService.removeItem(STORAGE_KEYS.USER_DATA);
    this.localStorageService.removeItem('projects');
    this.localStorageService.removeItem('project_zip_cache_v1');

    if (typeof window !== 'undefined' && window.localStorage) {
      window.localStorage.removeItem('access_token');
      window.localStorage.removeItem('refresh_token');
      window.localStorage.removeItem('user_data');
      window.localStorage.removeItem('projects');
      window.localStorage.removeItem('project_zip_cache_v1');
    }

    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);

    this.router.navigate(['/'], { replaceUrl: true }).then(() => {
      if (typeof window !== 'undefined') {
        window.history.pushState(null, '', window.location.href);
      }
    });
  }

  private handleAuthError(error: any): Observable<never> {
    return throwError(() => error);
  }

  isLoggedIn(): boolean {
    return !!this.getAccessToken() && !!this.currentUserValue;
  }

  getCurrentUser(): UserData | null {
    return this.currentUserValue;
  }

  getUserData(): UserData | null {
    const userData = this.localStorageService.getItem(STORAGE_KEYS.USER_DATA);
    return userData ? JSON.parse(userData) : null;
  }

  clearExpiredSession(): void {
    this.localStorageService.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
    this.localStorageService.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
    this.localStorageService.removeItem(STORAGE_KEYS.USER_DATA);
    this.localStorageService.removeItem('projects');
    this.localStorageService.removeItem('project_zip_cache_v1');
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
    this.router.navigate(['/'], { replaceUrl: true });
  }
}
