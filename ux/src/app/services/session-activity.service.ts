import { Injectable, NgZone, OnDestroy } from '@angular/core';
import { BehaviorSubject, EMPTY, Subscription, fromEvent, interval, merge } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from './auth.service';
import { ToastService } from './toast.service';

interface SessionWarningState {
  visible: boolean;
  countdownSeconds: number;
}

@Injectable({
  providedIn: 'root'
})
export class SessionActivityService implements OnDestroy {
  private static readonly IDLE_THRESHOLD_MS = 5 * 60 * 1000;
  private static readonly WARNING_DURATION_SECONDS = 3 * 60;
  private static readonly REFRESH_LEAD_MS = 2 * 60 * 1000;
  private static readonly ACTIVITY_EVENTS = ['mousemove', 'mousedown', 'keydown', 'scroll', 'touchstart', 'click'];

  private readonly warningStateSubject = new BehaviorSubject<SessionWarningState>({
    visible: false,
    countdownSeconds: SessionActivityService.WARNING_DURATION_SECONDS
  });
  readonly warningState$ = this.warningStateSubject.asObservable();

  private activitySubscription: Subscription | null = null;
  private monitorSubscription: Subscription | null = null;
  private lastActivityAt = Date.now();
  private warningStartedAt: number | null = null;
  private extendInProgress = false;

  constructor(
    private authService: AuthService,
    private toastService: ToastService,
    private ngZone: NgZone
  ) {}

  start(): void {
    if (typeof window === 'undefined' || this.activitySubscription || this.monitorSubscription) {
      return;
    }

    this.lastActivityAt = Date.now();
    this.ngZone.runOutsideAngular(() => {
      this.activitySubscription = merge(
        ...SessionActivityService.ACTIVITY_EVENTS.map((eventName) => fromEvent(window, eventName))
      ).subscribe(() => {
        this.ngZone.run(() => this.handleActivity());
      });

      this.monitorSubscription = interval(1000).subscribe(() => {
        this.ngZone.run(() => this.monitorSession());
      });
    });
  }

  stop(): void {
    this.activitySubscription?.unsubscribe();
    this.activitySubscription = null;
    this.monitorSubscription?.unsubscribe();
    this.monitorSubscription = null;
    this.warningStartedAt = null;
    this.warningStateSubject.next({
      visible: false,
      countdownSeconds: SessionActivityService.WARNING_DURATION_SECONDS
    });
  }

  extendSession(): void {
    if (this.extendInProgress) {
      return;
    }

    this.extendInProgress = true;
    this.authService.refreshToken().pipe(
      catchError(() => {
        this.toastService.error('Failed to extend session.');
        return EMPTY;
      })
    ).subscribe({
      next: () => {
        this.extendInProgress = false;
        this.lastActivityAt = Date.now();
        this.warningStartedAt = null;
        this.warningStateSubject.next({
          visible: false,
          countdownSeconds: SessionActivityService.WARNING_DURATION_SECONDS
        });
      },
      complete: () => {
        this.extendInProgress = false;
      }
    });
  }

  expireSessionNow(): void {
    this.authService.clearExpiredSession();
    this.warningStartedAt = null;
    this.warningStateSubject.next({
      visible: false,
      countdownSeconds: SessionActivityService.WARNING_DURATION_SECONDS
    });
  }

  ngOnDestroy(): void {
    this.stop();
  }

  private handleActivity(): void {
    this.lastActivityAt = Date.now();
    if (!this.warningStateSubject.value.visible) {
      return;
    }
    this.warningStateSubject.next({
      ...this.warningStateSubject.value,
      countdownSeconds: this.warningStateSubject.value.countdownSeconds
    });
  }

  private monitorSession(): void {
    if (!this.authService.isLoggedIn()) {
      this.warningStartedAt = null;
      if (this.warningStateSubject.value.visible) {
        this.warningStateSubject.next({
          visible: false,
          countdownSeconds: SessionActivityService.WARNING_DURATION_SECONDS
        });
      }
      return;
    }

    const now = Date.now();
    const idleForMs = now - this.lastActivityAt;
    const isIdle = idleForMs >= SessionActivityService.IDLE_THRESHOLD_MS;

    if (!isIdle) {
      this.warningStartedAt = null;
      if (this.warningStateSubject.value.visible) {
        this.warningStateSubject.next({
          visible: false,
          countdownSeconds: SessionActivityService.WARNING_DURATION_SECONDS
        });
      }
      this.refreshIfNeeded(now);
      return;
    }

    if (this.warningStartedAt === null) {
      this.warningStartedAt = now;
    }

    const elapsedWarningSeconds = Math.floor((now - this.warningStartedAt) / 1000);
    const countdownSeconds = Math.max(0, SessionActivityService.WARNING_DURATION_SECONDS - elapsedWarningSeconds);
    this.warningStateSubject.next({
      visible: true,
      countdownSeconds
    });

    if (countdownSeconds === 0) {
      this.authService.clearExpiredSession();
      this.stop();
      this.toastService.error('Session expired due to inactivity.');
    }
  }

  private refreshIfNeeded(now: number): void {
    const expiration = this.authService.getAccessTokenExpiration();
    if (!expiration || this.extendInProgress) {
      return;
    }

    if (expiration - now > SessionActivityService.REFRESH_LEAD_MS) {
      return;
    }

    this.extendInProgress = true;
    this.authService.refreshToken().subscribe({
      next: () => {
        this.extendInProgress = false;
      },
      error: () => {
        this.extendInProgress = false;
      }
    });
  }
}
