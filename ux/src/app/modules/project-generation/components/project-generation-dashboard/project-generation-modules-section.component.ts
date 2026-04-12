import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { ModulesSelectionComponent } from '../modules-selection/modules-selection.component';
import type { ProjectGenerationDashboardComponent } from './project-generation-dashboard.component';

@Component({
  selector: 'app-project-generation-modules-section',
  standalone: true,
  imports: [CommonModule, MatButtonModule, ModulesSelectionComponent],
  templateUrl: './project-generation-modules-section.component.html',
  styleUrls: ['./project-generation-modules-section.component.css']
})
export class ProjectGenerationModulesSectionComponent {
  @Input({ required: true }) host!: ProjectGenerationDashboardComponent;
}
