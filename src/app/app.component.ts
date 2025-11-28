import { Component, OnInit } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { IonApp } from '@ionic/angular/standalone';
import { HeaderComponent } from './components/header/header.component';
import { FooterComponent } from './components/footer/footer.component';
import { ToastComponent } from './components/toast/toast.component';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs/operators';
import { HttpClientModule } from '@angular/common/http';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, IonApp, RouterOutlet, HeaderComponent, FooterComponent, ToastComponent, HttpClientModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'QuadProSol - IT Solutions';
  isNavigating = false;
  isDashboardRoute = false;

  constructor(
    private router: Router,
    private authService: AuthService
  ) {
    // Handle navigation loading state and scroll to top
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      this.isNavigating = true;
      this.isDashboardRoute = event.urlAfterRedirects.includes('/user/dashboard');

      // Hide loading after a short delay to ensure smooth transition
      setTimeout(() => {
        this.isNavigating = false;
      }, 300);

      this.scrollToTop();
    });
  }

  ngOnInit(): void {
    this.checkTokenExpiration();
  }

  private checkTokenExpiration(): void {
    const token = this.authService.getAccessToken();

    if (token) {
      if (!this.isTokenValid(token)) {
        this.authService.clearExpiredSession();
      }
    }
  }

  private isTokenValid(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expirationTime = payload.exp * 1000;
      return Date.now() < expirationTime;
    } catch (error) {
      return false;
    }
  }

  private scrollToTop(): void {
    if (typeof window !== 'undefined') {
      const mainContent = document.querySelector('.main-content-scrollable');
      if (mainContent) {
        mainContent.scrollTo({ top: 0, behavior: 'smooth' });
      }
    }
  }
}