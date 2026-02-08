import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-info-banner',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './info-banner.component.html',
  styleUrls: ['./info-banner.component.css']
})
export class InfoBannerComponent implements OnInit {
  @Input() storageKey = '';
  @Input() show = true;

  isVisible = true;

  ngOnInit(): void {
    this.clearDismissalsOnReload();
    const dismissed = this.storageKey
      ? sessionStorage.getItem(this.buildStorageKey())
      : null;
    this.isVisible = this.show && !dismissed;
  }

  close(): void {
    this.isVisible = false;
    if (this.storageKey) {
      sessionStorage.setItem(this.buildStorageKey(), 'dismissed');
    }
  }

  private buildStorageKey(): string {
    return `info-banner:${this.storageKey}`;
  }

  private isPageReload(): boolean {
    const entries = performance.getEntriesByType('navigation') as PerformanceNavigationTiming[];
    if (entries && entries.length > 0) {
      return entries[0].type === 'reload';
    }
    // Fallback for older browsers.
    // eslint-disable-next-line deprecation/deprecation
    return (performance as any).navigation?.type === 1;
  }

  private clearDismissalsOnReload(): void {
    if (!this.isPageReload()) {
      return;
    }
    const sessionId = String(performance.timeOrigin || Date.now());
    const storedSessionId = sessionStorage.getItem('info-banner:session-id');
    if (storedSessionId === sessionId) {
      return;
    }
    const keysToRemove: string[] = [];
    for (let i = 0; i < sessionStorage.length; i += 1) {
      const key = sessionStorage.key(i);
      if (key && key.startsWith('info-banner:') && key !== 'info-banner:session-id') {
        keysToRemove.push(key);
      }
    }
    keysToRemove.forEach(key => sessionStorage.removeItem(key));
    sessionStorage.setItem('info-banner:session-id', sessionId);
  }
}
