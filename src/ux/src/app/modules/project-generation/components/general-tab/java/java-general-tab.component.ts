import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { MatExpansionModule } from '@angular/material/expansion';
import { JavaDatabaseSettingsSectionComponent } from './java-database-settings-section.component';
import { JavaDeveloperPreferencesSectionComponent } from './java-developer-preferences-section.component';
import { JavaProjectSettingsSectionComponent } from './java-project-settings-section.component';

@Component({
  selector: 'app-java-general-tab',
  standalone: true,
  imports: [
    CommonModule,
    MatExpansionModule,
    JavaProjectSettingsSectionComponent,
    JavaDatabaseSettingsSectionComponent,
    JavaDeveloperPreferencesSectionComponent
  ],
  templateUrl: './java-general-tab.component.html',
  styleUrls: ['./general-tab-section.component.css']
})
export class JavaGeneralTabComponent {
  @Input({ required: true }) projectSettings!: any;
  @Input({ required: true }) databaseSettings!: any;
  @Input({ required: true }) developerPreferences!: any;
  @Input() projectGroupError = '';
  @Input() projectNameError = '';
  @Input({ required: true }) dbTypeOptions!: Array<'SQL' | 'NOSQL' | 'NONE'>;
  @Input({ required: true }) filteredDatabaseOptions!: Array<{ value: string; label: string; type: 'SQL' | 'NOSQL' }>;
  @Input({ required: true }) dbGenerationOptions!: string[];
  @Input({ required: true }) javaVersionOptions!: string[];
  @Input({ required: true }) showDbGeneration!: boolean;
  @Input({ required: true }) showPluralizeTableNames!: boolean;

  @Output() projectGroupChange = new EventEmitter<void>();
  @Output() projectNameChange = new EventEmitter<void>();
  @Output() databaseTypeChange = new EventEmitter<'SQL' | 'NOSQL' | 'NONE'>();
  @Output() databaseSelectionChange = new EventEmitter<string>();
  @Output() addProfile = new EventEmitter<void>();
  @Output() removeProfile = new EventEmitter<string>();
  @Output() enableActuatorChange = new EventEmitter<boolean>();
  @Output() configureApiChange = new EventEmitter<MatCheckboxChange>();
}
