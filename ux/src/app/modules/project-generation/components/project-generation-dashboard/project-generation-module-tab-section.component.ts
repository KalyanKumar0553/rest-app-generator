import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { AuthModuleTabComponent } from '../auth-module-tab/auth-module-tab.component';
import { RbacModuleTabComponent } from '../rbac-module-tab/rbac-module-tab.component';
import { StateMachineModuleTabComponent } from '../state-machine-module-tab/state-machine-module-tab.component';
import { SubscriptionModuleTabComponent } from '../subscription-module-tab/subscription-module-tab.component';
import { SwaggerModuleTabComponent } from '../swagger-module-tab/swagger-module-tab.component';
import type { ProjectGenerationDashboardComponent } from './project-generation-dashboard.component';

@Component({
  selector: 'app-project-generation-module-tab-section',
  standalone: true,
  imports: [CommonModule, MatButtonModule, RbacModuleTabComponent, AuthModuleTabComponent, StateMachineModuleTabComponent, SubscriptionModuleTabComponent, SwaggerModuleTabComponent],
  templateUrl: './project-generation-module-tab-section.component.html',
  styleUrls: ['./project-generation-module-tab-section.component.css']
})
export class ProjectGenerationModuleTabSectionComponent {
  @Input({ required: true }) host!: ProjectGenerationDashboardComponent;
  @Input({ required: true }) moduleKey!: string;
}
