import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { InfoBannerComponent } from '../../../../components/info-banner/info-banner.component';
import { ActuatorConfigComponent } from '../actuator-config/actuator-config.component';
import type { ProjectGenerationDashboardComponent } from './project-generation-dashboard.component';

@Component({
  selector: 'app-project-generation-actuator-section',
  standalone: true,
  imports: [CommonModule, MatButtonModule, InfoBannerComponent, ActuatorConfigComponent],
  templateUrl: './project-generation-actuator-section.component.html',
  styleUrls: ['./project-generation-actuator-section.component.css']
})
export class ProjectGenerationActuatorSectionComponent {
  @Input({ required: true }) host!: ProjectGenerationDashboardComponent;
}
