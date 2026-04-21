import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  NgZone,
  OnDestroy,
  Output,
  ViewChild
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { environment } from '../../../environments/environment';

declare global {
  interface Window {
    turnstile?: {
      render: (
        container: HTMLElement,
        options: {
          sitekey: string;
          callback?: (token: string) => void;
          'expired-callback'?: () => void;
          'error-callback'?: () => void;
          theme?: 'light' | 'dark' | 'auto';
          size?: 'normal' | 'compact' | 'flexible';
        }
      ) => string;
      reset: (widgetId: string) => void;
      remove: (widgetId: string) => void;
    };
  }
}

@Component({
  selector: 'app-turnstile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './turnstile.component.html',
  styleUrls: ['./turnstile.component.css']
})
export class TurnstileComponent implements AfterViewInit, OnDestroy {
  @Input() theme: 'light' | 'dark' | 'auto' = 'auto';
  @Input() size: 'normal' | 'compact' | 'flexible' = 'flexible';
  @Output() tokenChange = new EventEmitter<string>();
  @Output() expired = new EventEmitter<void>();
  @Output() error = new EventEmitter<void>();

  @ViewChild('turnstileContainer') containerRef!: ElementRef<HTMLDivElement>;

  private widgetId: string | null = null;
  private pollTimer: any = null;
  private readonly MAX_POLL_MS = 15000;
  private pollElapsed = 0;
  hasError = false;

  constructor(private zone: NgZone) {}

  ngAfterViewInit(): void {
    this.renderWhenReady();
  }

  ngOnDestroy(): void {
    this.clearPoll();
    if (this.widgetId && window.turnstile) {
      try {
        window.turnstile.remove(this.widgetId);
      } catch {
        // ignore
      }
    }
  }

  reset(): void {
    if (this.widgetId && window.turnstile) {
      window.turnstile.reset(this.widgetId);
    }
  }

  private renderWhenReady(): void {
    if (window.turnstile) {
      this.renderWidget();
      return;
    }
    this.pollTimer = setInterval(() => {
      this.pollElapsed += 300;
      if (window.turnstile) {
        this.clearPoll();
        this.renderWidget();
      } else if (this.pollElapsed >= this.MAX_POLL_MS) {
        this.clearPoll();
        this.zone.run(() => {
          this.hasError = true;
        });
      }
    }, 300);
  }

  private renderWidget(): void {
    const sitekey = (environment as any).turnstileSiteKey || '1x00000000000000000000AA';
    this.widgetId = window.turnstile!.render(this.containerRef.nativeElement, {
      sitekey,
      theme: this.theme,
      size: this.size,
      callback: (token: string) => {
        this.zone.run(() => {
          this.hasError = false;
          this.tokenChange.emit(token);
        });
      },
      'expired-callback': () => {
        this.zone.run(() => {
          this.expired.emit();
          this.tokenChange.emit('');
        });
      },
      'error-callback': () => {
        this.zone.run(() => {
          this.hasError = true;
          this.error.emit();
        });
      }
    });
  }

  private clearPoll(): void {
    if (this.pollTimer) {
      clearInterval(this.pollTimer);
      this.pollTimer = null;
    }
  }
}
