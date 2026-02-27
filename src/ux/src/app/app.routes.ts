import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { TermsComponent } from './components/terms/terms.component';
import { PrivacyComponent } from './components/privacy/privacy.component';
import { InprogressComponent } from './components/inprogress/inprogress.component';
import { LoadingOverlayDemoComponent } from './components/shared/loading-overlay-demo/loading-overlay-demo.component';
import { DocumentationComponent } from './components/documentation/documentation.component';

export const routes: Routes = [
  {
    path: '',
    component: HomeComponent,
    pathMatch: 'full'
  },
  {
    path: 'auth',
    loadChildren: () => import('./modules/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },
  {
    path: 'user',
    loadChildren: () => import('./modules/user/user.routes').then(m => m.USER_ROUTES)
  },
  {
    path: 'project-generation',
    loadChildren: () => import('./modules/project-generation/project-generation.routes').then(m => m.PROJECT_GENERATION_ROUTES)
  },
  {
    path: 'dashboard',
    redirectTo: 'user/dashboard',
    pathMatch: 'full'
  },
  {
    path: 'terms',
    component: TermsComponent
  },
  {
    path: 'privacy',
    component: PrivacyComponent
  },
  {
    path: 'in-progress',
    component: InprogressComponent
  },
  {
    path: 'overlay-demo',
    component: LoadingOverlayDemoComponent
  },
  {
    path: 'documentation',
    component: DocumentationComponent
  },
  {
    path: '**',
    redirectTo: ''
  }
];
