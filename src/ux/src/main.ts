import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter, withRouterConfig, withHashLocation } from '@angular/router';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideIonicAngular } from '@ionic/angular/standalone';
import { provideHttpClient, withInterceptors, HTTP_INTERCEPTORS } from '@angular/common/http';
import { routes } from './app/app.routes';
import { AppComponent } from './app/app.component';
import { HttpRequestInterceptor } from './app/interceptors/http.interceptor';

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes, withHashLocation(), withRouterConfig({ onSameUrlNavigation: 'reload' })),
    provideAnimations(),
    provideIonicAngular({}),
    provideHttpClient(),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpRequestInterceptor,
      multi: true
    }
  ]
}).catch(err => console.error(err));