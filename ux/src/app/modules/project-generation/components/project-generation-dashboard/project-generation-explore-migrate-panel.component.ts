import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import type { ProjectGenerationDashboardComponent } from './project-generation-dashboard.component';
import type { NodeProjectGenerationDashboardComponent } from '../node-project-generation-dashboard/node-project-generation-dashboard.component';

@Component({
  selector: 'app-project-generation-explore-migrate-panel',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule],
  templateUrl: './project-generation-explore-migrate-panel.component.html',
  styleUrls: ['./project-generation-explore-migrate-panel.component.css']
})
export class ProjectGenerationExploreMigratePanelComponent {
  @Input({ required: true }) host!: ProjectGenerationDashboardComponent | NodeProjectGenerationDashboardComponent;
}
