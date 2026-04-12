import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { DataObjectsComponent } from '../data-objects/data-objects.component';
import type { ProjectGenerationDashboardComponent } from './project-generation-dashboard.component';

@Component({
  selector: 'app-project-generation-data-objects-section',
  standalone: true,
  imports: [CommonModule, MatButtonModule, DataObjectsComponent],
  templateUrl: './project-generation-data-objects-section.component.html',
  styleUrls: ['./project-generation-data-objects-section.component.css']
})
export class ProjectGenerationDataObjectsSectionComponent {
  @Input({ required: true }) host!: ProjectGenerationDashboardComponent;
}
