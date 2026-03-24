import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';
import { JavaDatabaseSettingsSectionComponent } from './java-database-settings-section.component';
import { JavaDeveloperPreferencesSectionComponent } from './java-developer-preferences-section.component';
import { JavaProjectSettingsSectionComponent } from './java-project-settings-section.component';

@Component({
  selector: 'app-java-general-tab',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatTooltipModule,
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
  @Input() projectDescriptionError = '';
  @Input({ required: true }) dbTypeOptions!: Array<'SQL' | 'NOSQL' | 'NONE'>;
  @Input({ required: true }) filteredDatabaseOptions!: Array<{ value: string; label: string; type: 'SQL' | 'NOSQL' }>;
  @Input({ required: true }) dbGenerationOptions!: string[];
  @Input({ required: true }) javaVersionOptions!: string[];
  @Input({ required: true }) showDbGeneration!: boolean;
  @Input({ required: true }) showPluralizeTableNames!: boolean;
  @Input() backendProjectId: string | null = null;
  @Input() contributorUserId = '';
  @Input() projectContributors: Array<{ userId: string }> = [];
  @Input() isContributorSaving = false;
  @Input() canManageContributors = false;
  @Input() dependencyInput = '';
  @Input() filteredDependencies: string[] = [];
  @Input() selectedDependencies: string[] = [];
  @Input() dependencyTooltipMessage = '';

  @Output() projectGroupChange = new EventEmitter<void>();
  @Output() projectNameChange = new EventEmitter<void>();
  @Output() projectDescriptionChange = new EventEmitter<void>();
  @Output() databaseTypeChange = new EventEmitter<'SQL' | 'NOSQL' | 'NONE'>();
  @Output() databaseSelectionChange = new EventEmitter<string>();
  @Output() addProfile = new EventEmitter<void>();
  @Output() removeProfile = new EventEmitter<string>();
  @Output() enableActuatorChange = new EventEmitter<boolean>();
  @Output() configureApiChange = new EventEmitter<MatCheckboxChange>();
  @Output() contributorUserIdChange = new EventEmitter<string>();
  @Output() addContributor = new EventEmitter<void>();
  @Output() removeContributor = new EventEmitter<string>();
  @Output() dependencyInputChange = new EventEmitter<string>();
  @Output() dependencySearchChange = new EventEmitter<string>();
  @Output() dependencySelected = new EventEmitter<MatAutocompleteSelectedEvent>();
  @Output() dependencyRemoved = new EventEmitter<string>();

  onContributorUserIdChange(value: string): void {
    this.contributorUserIdChange.emit(value);
  }

  onDependencyInputChange(value: string): void {
    this.dependencyInputChange.emit(value);
    this.dependencySearchChange.emit(value);
  }
}
