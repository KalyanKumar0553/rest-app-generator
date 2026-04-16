import { Component, OnDestroy, OnInit } from '@angular/core';
import { stableArray, emptyCache } from './utils/stable-reference';
import { RouterOutlet, Router, NavigationEnd, ActivatedRouteSnapshot } from '@angular/router';
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
      this.requestLoadingService.setRouteEnabled(this.isRequestOverlayEnabledForCurrentRoute());

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
    this.requestLoadingService.setRouteEnabled(this.isRequestOverlayEnabledForCurrentRoute());
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

  private isRequestOverlayEnabledForCurrentRoute(): boolean {
    return this.resolveRequestOverlayForRoute(this.router.routerState.snapshot.root, false);
  }

  private resolveRequestOverlayForRoute(snapshot: ActivatedRouteSnapshot | null, inheritedEnabled: boolean): boolean {
    if (!snapshot) {
      return inheritedEnabled;
    }

    const explicitFlag = snapshot.data?.['requestLoadingOverlay'];
    const localEnabled = typeof explicitFlag === 'boolean' ? explicitFlag : inheritedEnabled;
    const applyAll = snapshot.data?.['requestLoadingOverlayApplyAll'] === true;
    const nextInheritedEnabled = applyAll ? localEnabled : false;
    const primaryChild = snapshot.children.find((child) => child.outlet === 'primary') ?? null;

    if (!primaryChild) {
      return localEnabled;
    }

    return this.resolveRequestOverlayForRoute(primaryChild, nextInheritedEnabled);
  }

  private _sessionWarningCache = emptyCache<string[]>();
  private _sessionWarningCachedSeconds = -1;
  get sessionWarningMessages(): string[] {
    if (this.sessionCountdownSeconds !== this._sessionWarningCachedSeconds) {
      this._sessionWarningCachedSeconds = this.sessionCountdownSeconds;
      const minutes = Math.floor(this.sessionCountdownSeconds / 60);
      const seconds = this.sessionCountdownSeconds % 60;
      const formattedSeconds = seconds.toString().padStart(2, '0');
      this._sessionWarningCache.ref = [
        `You have been idle for 5 minutes. Your session will expire in ${minutes}:${formattedSeconds}.`,
        'Extend the session to continue working from the same place.'
      ];
      this._sessionWarningCache.json = JSON.stringify(this._sessionWarningCache.ref);
    }
    return this._sessionWarningCache.ref;
  }

  extendSession(): void {
    this.sessionActivityService.extendSession();
  }

  logoutFromSessionWarning(): void {
    this.sessionActivityService.expireSessionNow();
    this.router.navigate(['/'], { replaceUrl: true });
  }
}
