import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RequestLoadingService {
  private readonly visibleSubject = new BehaviorSubject<boolean>(false);
  readonly visible$ = this.visibleSubject.asObservable();

  private activeRequests = 0;
  private showTimer: ReturnType<typeof setTimeout> | null = null;
  private hideTimer: ReturnType<typeof setTimeout> | null = null;

  begin(): void {
    this.activeRequests += 1;

    if (this.hideTimer) {
      clearTimeout(this.hideTimer);
      this.hideTimer = null;
    }

    if (this.visibleSubject.value || this.showTimer) {
      return;
    }

    this.showTimer = setTimeout(() => {
      this.showTimer = null;
      if (this.activeRequests > 0) {
        this.visibleSubject.next(true);
      }
    }, 120);
  }

  end(): void {
    this.activeRequests = Math.max(0, this.activeRequests - 1);

    if (this.activeRequests > 0) {
      return;
    }

    if (this.showTimer) {
      clearTimeout(this.showTimer);
      this.showTimer = null;
    }

    if (this.hideTimer) {
      clearTimeout(this.hideTimer);
    }

    this.hideTimer = setTimeout(() => {
      this.hideTimer = null;
      if (this.activeRequests === 0) {
        this.visibleSubject.next(false);
      }
    }, 180);
  }
}
