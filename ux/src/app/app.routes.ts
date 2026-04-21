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
    data: {
      requestLoadingOverlay: true,
      requestLoadingOverlayApplyAll: true
    },
    loadChildren: () => import('./modules/user/user.routes').then(m => m.USER_ROUTES)
  },
  {
    path: 'project-generation',
    data: {
      requestLoadingOverlay: true,
      requestLoadingOverlayApplyAll: true
    },
    loadChildren: () => import('./modules/project-generation/project-generation.routes').then(m => m.PROJECT_GENERATION_ROUTES)
  },
  {
    path: 'project-generation-node',
    data: {
      language: 'node',
      requestLoadingOverlay: true,
      requestLoadingOverlayApplyAll: true
    },
    loadComponent: () => import('./modules/project-generation/components/node-project-generation-dashboard/node-project-generation-dashboard.component')
      .then(m => m.NodeProjectGenerationDashboardComponent)
  },
  {
    path: 'project-generation-python',
    data: {
      language: 'python',
      requestLoadingOverlay: true,
      requestLoadingOverlayApplyAll: true
    },
    loadComponent: () => import('./modules/project-generation/components/node-project-generation-dashboard/node-project-generation-dashboard.component')
      .then(m => m.NodeProjectGenerationDashboardComponent)
  },
  {
    path: 'project-collaboration/:inviteToken',
    loadComponent: () => import('./modules/project-generation/components/project-collaboration-invite/project-collaboration-invite.component')
      .then(m => m.ProjectCollaborationInviteComponent)
  },
  {
    path: 'checkout',
    loadChildren: () => import('./modules/checkout/checkout.routes').then(m => m.CHECKOUT_ROUTES)
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
