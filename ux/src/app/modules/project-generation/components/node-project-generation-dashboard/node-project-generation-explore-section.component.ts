import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { HelpPopoverComponent, HelpPopoverTriggerDirective } from '../../../../components/help-popover/help-popover.component';
import { NodeProjectViewComponent } from '../node/project-view/node-project-view.component';
import { ProjectGenerationExploreMigratePanelComponent } from '../project-generation-dashboard/project-generation-explore-migrate-panel.component';
import type { NodeProjectGenerationDashboardComponent } from './node-project-generation-dashboard.component';

@Component({
  selector: 'app-node-project-generation-explore-section',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatTooltipModule,
    HelpPopoverComponent,
    HelpPopoverTriggerDirective,
    NodeProjectViewComponent,
    ProjectGenerationExploreMigratePanelComponent
  ],
  templateUrl: './node-project-generation-explore-section.component.html',
  styleUrls: ['./node-project-generation-explore-section.component.css']
})
export class NodeProjectGenerationExploreSectionComponent {
  @Input({ required: true }) host!: NodeProjectGenerationDashboardComponent;
}
