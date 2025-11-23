import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { IntakeFormComponent } from './components/intake-form/intake-form.component';
import { CustomSoftwareComponent } from './components/custom-software/custom-software.component';
import { SchedulingComponent } from './components/scheduling/scheduling.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'intake', component: IntakeFormComponent },
  { path: 'custom-software', component: CustomSoftwareComponent },
  { path: 'scheduling', component: SchedulingComponent },
  { path: '**', redirectTo: '' }
];