import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, timer } from 'rxjs';
import { catchError, finalize, map, retry, tap, timeout } from 'rxjs/operators';
import { Router } from '@angular/router';
import { API_CONFIG, HTTP_STATUS, ERROR_MESSAGES, STORAGE_KEYS } from '../constants/api.constants';
import { LocalStorageService } from '../services/local-storage.service';
import { ToastService } from '../services/toast.service';
import { RequestLoadingService } from '../services/request-loading.service';
import { parseApiError } from '../utils/api-error.utils';

@Injectable()
export class HttpRequestInterceptor implements HttpInterceptor {
  constructor(
    private router: Router,
    private localStorageService: LocalStorageService,
    private toastService: ToastService,
    private requestLoadingService: RequestLoadingService
  ) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const modifiedRequest = this.addHeaders(request);
    const shouldTrackLoading = this.shouldTrackLoading(request);

    if (shouldTrackLoading) {
      this.requestLoadingService.begin();
    }

    return next.handle(modifiedRequest).pipe(
      timeout(API_CONFIG.TIMEOUT),
      tap(event => {
        if (event instanceof HttpResponse) {
          this.handleSuccessResponse(event);
        }
      }),
      catchError((error: HttpErrorResponse) => {
        return this.handleErrorResponse(error, request, next);
      }),
      finalize(() => {
        if (shouldTrackLoading) {
          this.requestLoadingService.end();
        }
      })
    );
  }

  private addHeaders(request: HttpRequest<any>): HttpRequest<any> {
    const token = this.localStorageService.getItem(STORAGE_KEYS.ACCESS_TOKEN);

    let headers = request.headers;

    if (!headers.has('Accept')) {
      headers = headers.set('Accept', 'application/json');
    }

    const isFormData = typeof FormData !== 'undefined' && request.body instanceof FormData;
    if (!headers.has('Content-Type') && request.body != null && !isFormData) {
      headers = headers.set('Content-Type', 'application/json');
    }

    if (token && !this.isPublicEndpoint(request)) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }

    return request.clone({ headers });
  }

  private isPublicEndpoint(request: HttpRequest<any>): boolean {
    const url = request.url;
    const method = request.method.toUpperCase();

    const publicRoutes: Array<{ method: string; path: string }> = [
      { method: 'GET', path: '/api/v1/auth/captcha' },
      { method: 'POST', path: '/api/v1/auth/login' },
      { method: 'GET', path: '/api/v1/auth/providers' },
      { method: 'POST', path: '/api/v1/auth/signup' },
      { method: 'POST', path: '/api/v1/auth/password/forgot' },
      { method: 'POST', path: '/api/v1/auth/otp/generate' },
      { method: 'POST', path: '/api/v1/auth/otp/verify' },
      { method: 'POST', path: '/api/v1/auth/password/reset' },
      { method: 'POST', path: '/api/v1/auth/token/refresh' },
      { method: 'POST', path: '/api/project-view/generate-zip' },
      { method: 'GET', path: '/api/openapi/dependencies' },
      { method: 'POST', path: '/api/analytics/visits/home' },
      { method: 'POST', path: '/api/v1/checkout/otp/send' },
      { method: 'POST', path: '/api/v1/checkout/otp/resend' },
      { method: 'POST', path: '/api/v1/checkout/otp/verify' },
      { method: 'POST', path: '/api/v1/orders' }
    ];

    return publicRoutes.some(route => method === route.method && url.includes(route.path));
  }

  private shouldTrackLoading(request: HttpRequest<any>): boolean {
    const url = request.url || '';
    if (!url.includes('/api/')) {
      return false;
    }

    if (!this.requestLoadingService.isRouteEnabled()) {
      return false;
    }

    const nonBlockingPaths = [
      '/api/analytics/visits/home'
    ];

    const isPresenceHeartbeat = url.includes('/api/projects/') && url.includes('/collaboration/presence/');
    if (isPresenceHeartbeat) {
      return false;
    }

    return !nonBlockingPaths.some(path => url.includes(path));
  }

  private handleSuccessResponse(response: HttpResponse<any>): void {
    if (response.body?.message && response.status === HTTP_STATUS.CREATED) {
      console.log('Success:', response.body.message);
    }
  }

  private handleErrorResponse(
    error: HttpErrorResponse,
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<never> {
    let errorMessage = ERROR_MESSAGES.UNKNOWN_ERROR;
    const parsedApiError = parseApiError(error, ERROR_MESSAGES.SERVER_ERROR);

    if (error.error instanceof ErrorEvent) {
      errorMessage = ERROR_MESSAGES.NETWORK_ERROR;
      console.error('Client-side error:', error.error.message);
    } else if (error.status === 0 && error.statusText === 'Unknown Error') {
      errorMessage = ERROR_MESSAGES.TIMEOUT_ERROR;
    } else {
      errorMessage = parsedApiError.userMessage;

      switch (error.status) {
        case HTTP_STATUS.UNAUTHORIZED:
          this.handleUnauthorized();
          break;
        case HTTP_STATUS.FORBIDDEN:
          errorMessage = ERROR_MESSAGES.FORBIDDEN;
          break;
        case HTTP_STATUS.NOT_FOUND:
          errorMessage = ERROR_MESSAGES.NOT_FOUND;
          break;
        case HTTP_STATUS.BAD_REQUEST:
        case HTTP_STATUS.UNPROCESSABLE_ENTITY:
          errorMessage = parsedApiError.userMessage || ERROR_MESSAGES.VALIDATION_ERROR;
          break;
        case HTTP_STATUS.INTERNAL_SERVER_ERROR:
        case HTTP_STATUS.SERVICE_UNAVAILABLE:
          errorMessage = parsedApiError.userMessage || ERROR_MESSAGES.SERVER_ERROR;
          break;
        case 0:
          errorMessage = ERROR_MESSAGES.NETWORK_ERROR;
          break;
      }

      console.error(`Server-side error: ${error.status}`, error.error);
    }

    return throwError(() => ({
      status: error.status,
      message: errorMessage,
      errorCode: parsedApiError.errorCode,
      exceptionType: parsedApiError.exceptionType,
      originalError: error
    }));
  }

  private getServerErrorMessage(error: HttpErrorResponse): string {
    return parseApiError(error, ERROR_MESSAGES.SERVER_ERROR).userMessage;
  }

  private handleUnauthorized(): void {
    this.localStorageService.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
    this.localStorageService.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
    this.localStorageService.removeItem(STORAGE_KEYS.USER_DATA);

    if (!this.router.url.includes('/login')) {
      this.router.navigate(['/']);
      this.toastService.error(ERROR_MESSAGES.UNAUTHORIZED);
    }
  }
}
