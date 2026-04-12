import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router,
    private toastService: ToastService
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    const token = this.authService.getAccessToken();

    if (!token) {
      this.toastService.error('Please login to access this page');
      return this.router.createUrlTree(['/'], {
        queryParams: { returnUrl: state.url }
      });
    }

    if (!this.isTokenValid(token)) {
      this.authService.clearExpiredSession();
      this.toastService.error('Your session has expired. Please login again.');
      return this.router.createUrlTree(['/'], {
        queryParams: { returnUrl: state.url }
      });
    }

    if (!this.authService.currentUserValue) {
      this.toastService.error('Please login to access this page');
      return this.router.createUrlTree(['/'], {
        queryParams: { returnUrl: state.url }
      });
    }

    return true;
  }

  private isTokenValid(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expirationTime = payload.exp * 1000;
      return Date.now() < expirationTime;
    } catch (error) {
      console.error('Invalid token format:', error);
      return false;
    }
  }
}
