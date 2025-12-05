import { Injectable } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { delay } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MockApiService {
  private useMockData = environment.useMockApi;

  constructor(private http: HttpClient) {}

  setUseMockData(use: boolean): void {
    this.useMockData = use;
  }

  get<T>(url: string, mockDataPath: string): Observable<T> {
    if (this.useMockData) {
      return this.http.get<T>(mockDataPath).pipe(delay(800));
    }
    return this.http.get<T>(url);
  }

  post<T>(url: string, body: any, mockDataPath: string): Observable<T> {
    if (this.useMockData) {
      if (this.shouldReturnError(url, body)) {
        return throwError(() => ({
          message: this.getErrorMessage(url, body),
          status: 400
        })).pipe(delay(800));
      }
      return this.http.get<T>(mockDataPath).pipe(delay(800));
    }
    return this.http.post<T>(url, body);
  }

  private shouldReturnError(url: string, body: any): boolean {
    if (url.includes('/verify-otp') && body.otp === '000000') {
      return true;
    }
    if (url.includes('/login') && body.email === 'invalid@example.com') {
      return true;
    }
    return false;
  }

  private getErrorMessage(url: string, body: any): string {
    if (url.includes('/verify-otp')) {
      return 'Invalid OTP. Please try again.';
    }
    if (url.includes('/login')) {
      return 'Invalid email or password.';
    }
    return 'An error occurred. Please try again.';
  }
}
