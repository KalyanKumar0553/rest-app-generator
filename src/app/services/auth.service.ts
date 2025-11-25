import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { LocalStorageService } from './local-storage.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(
    private http: HttpClient,
    private localStorageService: LocalStorageService
  ) { }

  validateToken(token: string): Observable<boolean> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    return this.http.get(`${this.apiUrl}/auth/validate`, { headers }).pipe(
      map(() => true),
      catchError(() => of(false))
    );
  }

  isAuthenticated(): boolean {
    const userDetails = this.localStorageService.get('userDetails');
    return userDetails && userDetails.token;
  }

  getUserDetails(): any {
    return this.localStorageService.get('userDetails');
  }

  logout(): void {
    this.localStorageService.clear();
  }
}
