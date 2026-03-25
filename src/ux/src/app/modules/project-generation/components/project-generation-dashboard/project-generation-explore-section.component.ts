import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ProjectViewComponent } from '../project-view/project-view.component';
import { ProjectGenerationExploreMigratePanelComponent } from './project-generation-explore-migrate-panel.component';
import type { ProjectGenerationDashboardComponent } from './project-generation-dashboard.component';

@Component({
  selector: 'app-project-generation-explore-section',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatTableModule, MatTooltipModule, ProjectViewComponent, ProjectGenerationExploreMigratePanelComponent],
  templateUrl: './project-generation-explore-section.component.html',
  styleUrls: ['./project-generation-explore-section.component.css']
})
export class ProjectGenerationExploreSectionComponent {
  @Input({ required: true }) host!: ProjectGenerationDashboardComponent;
}
