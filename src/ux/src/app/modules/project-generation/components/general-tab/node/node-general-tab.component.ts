import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { MatExpansionModule } from '@angular/material/expansion';
import { NodeDatabaseSettingsSectionComponent } from './node-database-settings-section.component';
import { NodeDeveloperPreferencesSectionComponent } from './node-developer-preferences-section.component';
import { NodeProjectSettingsSectionComponent } from './node-project-settings-section.component';

@Component({
  selector: 'app-node-general-tab',
  standalone: true,
  imports: [
    CommonModule,
    MatExpansionModule,
    NodeProjectSettingsSectionComponent,
    NodeDatabaseSettingsSectionComponent,
    NodeDeveloperPreferencesSectionComponent
  ],
  templateUrl: './node-general-tab.component.html',
  styleUrls: ['./general-tab-section.component.css']
})
export class NodeGeneralTabComponent {
  @Input({ required: true }) projectSettings!: any;
  @Input({ required: true }) databaseSettings!: any;
  @Input({ required: true }) developerPreferences!: any;
  @Input() projectGroupError = '';
  @Input() projectNameError = '';
  @Input() projectDescriptionError = '';
  @Input({ required: true }) dbTypeOptions!: Array<'SQL' | 'NOSQL' | 'NONE'>;
  @Input({ required: true }) filteredDatabaseOptions!: Array<{ value: string; label: string; type: 'SQL' | 'NOSQL' }>;

  @Output() projectGroupChange = new EventEmitter<void>();
  @Output() projectNameChange = new EventEmitter<void>();
  @Output() projectDescriptionChange = new EventEmitter<void>();
  @Output() databaseTypeChange = new EventEmitter<'SQL' | 'NOSQL' | 'NONE'>();
  @Output() databaseSelectionChange = new EventEmitter<string>();
  @Output() addProfile = new EventEmitter<void>();
  @Output() removeProfile = new EventEmitter<string>();
  @Output() configureApiChange = new EventEmitter<MatCheckboxChange>();

  readonly packageManagerOptions = ['npm', 'pnpm', 'yarn'];
}
