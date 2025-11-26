import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { TermsComponent } from './components/terms/terms.component';
import { PrivacyComponent } from './components/privacy/privacy.component';

export const routes: Routes = [
  {
    path: '',
    component: HomeComponent
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
    path: '**',
    redirectTo: ''
  }
];
