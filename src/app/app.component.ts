import { Component } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { IonApp, IonRouterOutlet, IonContent } from '@ionic/angular/standalone';
import { HeaderComponent } from './components/header/header.component';
import { FooterComponent } from './components/footer/footer.component';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, IonApp, IonRouterOutlet, IonContent, RouterOutlet, HeaderComponent, FooterComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
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