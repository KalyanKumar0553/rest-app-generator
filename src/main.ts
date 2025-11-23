import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter, RouterOutlet, withRouterConfig, withHashLocation } from '@angular/router';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideIonicAngular } from '@ionic/angular/standalone';
import { routes } from './app/app.routes';
import { AppComponent } from './app/app.component';

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes, withHashLocation(), withRouterConfig({ onSameUrlNavigation: 'reload' })),
    provideAnimations(),
    provideIonicAngular({})
  ]
}).catch(err => console.error(err));