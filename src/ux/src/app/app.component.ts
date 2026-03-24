import { Component, OnDestroy, OnInit } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { IonApp } from '@ionic/angular/standalone';
import { HeaderComponent } from './components/header/header.component';
import { FooterComponent } from './components/footer/footer.component';
import { ToastComponent } from './components/toast/toast.component';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs/operators';
import { HttpClientModule } from '@angular/common/http';
import { AuthService } from './services/auth.service';
import { ThemeService } from './services/theme.service';
import { LoadingOverlayComponent } from './components/shared/loading-overlay/loading-overlay.component';
import { RequestLoadingService } from './services/request-loading.service';
import { OauthProgressService } from './services/oauth-progress.service';
import { ConfirmationModalComponent, ModalButton } from './components/confirmation-modal/confirmation-modal.component';
import { SessionActivityService } from './services/session-activity.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, IonApp, RouterOutlet, HeaderComponent, FooterComponent, ToastComponent, HttpClientModule, LoadingOverlayComponent, ConfirmationModalComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'BootRid - Rapid API Developement Tool';
  isNavigating = false;
  isDashboardRoute = false;
  isProjectGenerationRoute = false;
  showSessionWarning = false;
  sessionCountdownSeconds = 180;
  readonly sessionWarningButtons: ModalButton[] = [
    { text: 'Logout', type: 'cancel', action: 'cancel' },
    { text: 'Extend Session', type: 'confirm', action: 'confirm' }
  ];
  private sessionWarningSubscription: Subscription | null = null;

  constructor(
    private router: Router,
    private authService: AuthService,
    private themeService: ThemeService,
    public requestLoadingService: RequestLoadingService,
    public oauthProgressService: OauthProgressService,
    private sessionActivityService: SessionActivityService
  ) {
    // Handle navigation loading state and scroll to top
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      this.isNavigating = true;
      this.isDashboardRoute = event.urlAfterRedirects.includes('/user/dashboard');
      this.isProjectGenerationRoute = event.urlAfterRedirects.includes('/project-generation');

      // Hide loading after a short delay to ensure smooth transition
      setTimeout(() => {
        this.isNavigating = false;
      }, 300);

      this.scrollToTop();
    });
  }

  ngOnInit(): void {
    this.checkTokenExpiration();
    this.themeService.getCurrentTheme();
    this.sessionActivityService.start();
    this.sessionWarningSubscription = this.sessionActivityService.warningState$.subscribe((state) => {
      this.showSessionWarning = state.visible;
      this.sessionCountdownSeconds = state.countdownSeconds;
    });
  }

  ngOnDestroy(): void {
    this.sessionWarningSubscription?.unsubscribe();
    this.sessionActivityService.stop();
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

  get sessionWarningMessages(): string[] {
    const minutes = Math.floor(this.sessionCountdownSeconds / 60);
    const seconds = this.sessionCountdownSeconds % 60;
    const formattedSeconds = seconds.toString().padStart(2, '0');
    return [
      `You have been idle for 5 minutes. Your session will expire in ${minutes}:${formattedSeconds}.`,
      'Extend the session to continue working from the same place.'
    ];
  }

  extendSession(): void {
    this.sessionActivityService.extendSession();
  }

  logoutFromSessionWarning(): void {
    this.sessionActivityService.expireSessionNow();
    this.router.navigate(['/'], { replaceUrl: true });
  }
}
