import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { AuthGuard } from '../../guards/auth.guard';

export const USER_ROUTES: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    children: [
      {
        path: 'dashboard',
        component: DashboardComponent,
        children: [
          {
            path: '',
            redirectTo: 'projects',
            pathMatch: 'full'
          },
          {
            path: 'projects',
            loadComponent: () => import('./components/projects-panel/projects-panel.component').then((m) => m.ProjectsPanelComponent)
          },
          {
            path: 'ai-labs',
            loadComponent: () => import('./components/ai-labs-panel/ai-labs-panel.component').then((m) => m.AiLabsPanelComponent)
          },
          {
            path: 'profile',
            loadComponent: () => import('./components/profile-panel/profile-panel.component').then((m) => m.ProfilePanelComponent)
          },
          {
            path: 'settings',
            loadComponent: () => import('./components/settings-panel/settings-panel.component').then((m) => m.SettingsPanelComponent)
          },
          {
            path: 'artifacts',
            loadComponent: () => import('./components/artifacts-panel/artifacts-panel.component').then((m) => m.ArtifactsPanelComponent)
          }
        ]
      },
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      }
    ]
  }
];
