import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, timer } from 'rxjs';
import { catchError, map, retry, tap, timeout } from 'rxjs/operators';
import { Router } from '@angular/router';
import { API_CONFIG, HTTP_STATUS, ERROR_MESSAGES, STORAGE_KEYS } from '../constants/api.constants';
import { LocalStorageService } from '../services/local-storage.service';
import { ToastService } from '../services/toast.service';

@Injectable()
export class HttpRequestInterceptor implements HttpInterceptor {
  constructor(
    private router: Router,
    private localStorageService: LocalStorageService,
    private toastService: ToastService
  ) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const modifiedRequest = this.addHeaders(request);

    return next.handle(modifiedRequest).pipe(
      timeout(API_CONFIG.TIMEOUT),
      tap(event => {
        if (event instanceof HttpResponse) {
          this.handleSuccessResponse(event);
        }
      }),
      catchError((error: HttpErrorResponse) => {
        return this.handleErrorResponse(error, request, next);
      })
    );
  }

  private addHeaders(request: HttpRequest<any>): HttpRequest<any> {
    const token = this.localStorageService.getItem(STORAGE_KEYS.ACCESS_TOKEN);

    let headers = request.headers
      .set('Content-Type', 'application/json')
      .set('Accept', 'application/json');

    if (token && !this.isPublicEndpoint(request.url)) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }

    return request.clone({ headers });
  }

  private isPublicEndpoint(url: string): boolean {
    const publicEndpoints = [
      '/api/auth/login',
      '/api/auth/signup',
      '/api/auth/forgot-password',
      '/api/auth/send-otp',
      '/api/auth/verify-otp',
      '/api/auth/reset-password-with-otp'
    ];
    return publicEndpoints.some(endpoint => url.includes(endpoint));
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

    if (error.error instanceof ErrorEvent) {
      errorMessage = ERROR_MESSAGES.NETWORK_ERROR;
      console.error('Client-side error:', error.error.message);
    } else if (error.status === 0 && error.statusText === 'Unknown Error') {
      errorMessage = ERROR_MESSAGES.TIMEOUT_ERROR;
    } else {
      errorMessage = this.getServerErrorMessage(error);

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
          errorMessage = error.error?.message || ERROR_MESSAGES.VALIDATION_ERROR;
          break;
        case HTTP_STATUS.INTERNAL_SERVER_ERROR:
        case HTTP_STATUS.SERVICE_UNAVAILABLE:
          errorMessage = ERROR_MESSAGES.SERVER_ERROR;
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
      originalError: error
    }));
  }

  private getServerErrorMessage(error: HttpErrorResponse): string {
    if (error.error?.message) {
      return error.error.message;
    }
    if (error.error?.error) {
      return error.error.error;
    }
    if (typeof error.error === 'string') {
      return error.error;
    }
    return ERROR_MESSAGES.SERVER_ERROR;
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
