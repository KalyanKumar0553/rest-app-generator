import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';

export const USER_ROUTES: Routes = [
  {
    path: '',
    children: [
      {
        path: 'dashboard',
        component: DashboardComponent
      },
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      }
    ]
  }
];
