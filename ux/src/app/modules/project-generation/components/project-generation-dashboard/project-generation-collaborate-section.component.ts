import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { InfoBannerComponent } from '../../../../components/info-banner/info-banner.component';
import type { ProjectGenerationDashboardComponent } from './project-generation-dashboard.component';

@Component({
  selector: 'app-project-generation-collaborate-section',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatTableModule,
    MatTooltipModule,
    InfoBannerComponent
  ],
  templateUrl: './project-generation-collaborate-section.component.html',
  styleUrls: ['./project-generation-collaborate-section.component.css']
})
export class ProjectGenerationCollaborateSectionComponent {
  @Input({ required: true }) host!: ProjectGenerationDashboardComponent;
}
