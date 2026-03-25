import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { EntitiesComponent } from '../entities/entities.component';
import type { ProjectGenerationDashboardComponent } from './project-generation-dashboard.component';

@Component({
  selector: 'app-project-generation-entities-section',
  standalone: true,
  imports: [CommonModule, MatButtonModule, EntitiesComponent],
  templateUrl: './project-generation-entities-section.component.html',
  styleUrls: ['./project-generation-entities-section.component.css']
})
export class ProjectGenerationEntitiesSectionComponent {
  @Input({ required: true }) host!: ProjectGenerationDashboardComponent;
}
