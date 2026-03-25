import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { InfoBannerComponent } from '../../../../components/info-banner/info-banner.component';
import { ControllersSpecTableComponent } from '../controllers-spec-table/controllers-spec-table.component';
import { RestConfigComponent } from '../rest-config/rest-config.component';
import type { ProjectGenerationDashboardComponent } from './project-generation-dashboard.component';

@Component({
  selector: 'app-project-generation-controllers-section',
  standalone: true,
  imports: [CommonModule, MatButtonModule, InfoBannerComponent, ControllersSpecTableComponent, RestConfigComponent],
  templateUrl: './project-generation-controllers-section.component.html',
  styleUrls: ['./project-generation-controllers-section.component.css']
})
export class ProjectGenerationControllersSectionComponent {
  @Input({ required: true }) host!: ProjectGenerationDashboardComponent;
}
