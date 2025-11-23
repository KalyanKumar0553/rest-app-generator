import { Component } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { IonApp, IonRouterOutlet, IonContent } from '@ionic/angular/standalone';
import { HeaderComponent } from './components/header/header.component';
import { FooterComponent } from './components/footer/footer.component';
import { CommonModule } from '@angular/common';
import { filter, delay } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, IonApp, IonRouterOutlet, IonContent, RouterOutlet, HeaderComponent, FooterComponent],
  template: `
    <ion-app>
      <ion-content class="app-content">
        <app-header></app-header>
        <main class="main-content" [class.loading]="isNavigating">
          <div class="loading-overlay" *ngIf="isNavigating">
            <div class="loading-spinner"></div>
          </div>
          <router-outlet></router-outlet>
        </main>
        <app-footer></app-footer>
      </ion-content>
    </ion-app>
  `,
  styles: [`
    .app-content {
      --background: transparent;
      --padding-top: 0;
      --padding-bottom: 0;
      --padding-start: 0;
      --padding-end: 0;
    }
    
    .main-content {
      min-height: calc(100vh - 160px);
      padding-top: 80px;
      position: relative;
      transition: opacity 0.3s ease;
    }
    
    .main-content.loading {
      opacity: 0.3;
    }
    
    .loading-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(255, 255, 255, 0.9);
      backdrop-filter: blur(5px);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 9999;
    }
    
    .loading-spinner {
      width: 40px;
      height: 40px;
      border: 3px solid var(--neutral-200);
      border-top: 3px solid var(--color-primary);
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }
    
    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
  `]
})
export class AppComponent {
  title = 'QuadProSol - IT Solutions';
  isNavigating = false;

  constructor(
    private router: Router
  ) {
    // Handle navigation loading state and scroll to top
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.isNavigating = true;
      
      // Hide loading after a short delay to ensure smooth transition
      setTimeout(() => {
        this.isNavigating = false;
      }, 300);
      
      this.scrollToTop();
    });
  }

  private scrollToTop(): void {
    // Try multiple scroll methods to ensure compatibility
    if (typeof window !== 'undefined') {
      // Method 1: Ionic content scroll
      const ionContent = document.querySelector('ion-content');
      if (ionContent) {
        ionContent.scrollToTop(300);
      }
      
      // Method 2: Window scroll (fallback)
      window.scrollTo({ top: 0, behavior: 'smooth' });
      
      // Method 3: Document scroll (additional fallback)
      document.documentElement.scrollTop = 0;
      document.body.scrollTop = 0;
    }
  }
}