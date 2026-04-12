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
  private readonly sessionIdKey = 'app:session-id';

  ngOnInit(): void {
    const dismissed = this.storageKey ? this.isDismissedForCurrentSession() : false;
    this.isVisible = this.show && !dismissed;
  }

  close(): void {
    this.isVisible = false;
    if (!this.storageKey || typeof window === 'undefined') {
      return;
    }

    const key = this.buildStorageKey();
    const sessionId = this.getOrCreateSessionId();
    if (!sessionId) {
      return;
    }

    window.localStorage.setItem(key, sessionId);
    window.sessionStorage.setItem(key, 'dismissed');
  }

  private buildStorageKey(): string {
    return `info-banner:${this.storageKey}`;
  }

  private isDismissedForCurrentSession(): boolean {
    if (typeof window === 'undefined') {
      return false;
    }

    const key = this.buildStorageKey();
    const sessionDismissed = window.sessionStorage.getItem(key) === 'dismissed';
    if (sessionDismissed) {
      return true;
    }

    const sessionId = this.getOrCreateSessionId();
    if (!sessionId) {
      return false;
    }

    const persistedSessionId = window.localStorage.getItem(key);
    return persistedSessionId === sessionId;
  }

  private getOrCreateSessionId(): string {
    if (typeof window === 'undefined') {
      return '';
    }

    const existing = window.sessionStorage.getItem(this.sessionIdKey);
    if (existing) {
      return existing;
    }

    const generated = `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
    window.sessionStorage.setItem(this.sessionIdKey, generated);
    return generated;
  }
}
