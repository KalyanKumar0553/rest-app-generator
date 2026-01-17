import { Routes } from '@angular/router';

export const PROJECT_GENERATION_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/project-generation-dashboard/project-generation-dashboard.component')
      .then(m => m.ProjectGenerationDashboardComponent)
  }
];
