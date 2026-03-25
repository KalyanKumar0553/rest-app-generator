import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { InfoBannerComponent } from '../../../../components/info-banner/info-banner.component';
import { JavaGeneralTabComponent } from '../general-tab/java/java-general-tab.component';
import type { ProjectGenerationDashboardComponent } from './project-generation-dashboard.component';

@Component({
  selector: 'app-project-generation-general-section',
  standalone: true,
  imports: [CommonModule, MatButtonModule, InfoBannerComponent, JavaGeneralTabComponent],
  templateUrl: './project-generation-general-section.component.html',
  styleUrls: ['./project-generation-general-section.component.css']
})
export class ProjectGenerationGeneralSectionComponent {
  @Input({ required: true }) host!: ProjectGenerationDashboardComponent;
}
