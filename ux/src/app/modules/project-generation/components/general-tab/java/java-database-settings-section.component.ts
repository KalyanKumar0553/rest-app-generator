import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-java-database-settings-section',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCheckboxModule, MatExpansionModule, MatFormFieldModule, MatIconModule, MatSelectModule, MatTooltipModule],
  templateUrl: './java-database-settings-section.component.html',
  styleUrls: ['./general-tab-section.component.css']
})
export class JavaDatabaseSettingsSectionComponent {
  @Input({ required: true }) databaseSettings!: any;
  @Input({ required: true }) dbTypeOptions!: Array<'SQL' | 'NOSQL' | 'NONE'>;
  @Input({ required: true }) filteredDatabaseOptions!: Array<{ value: string; label: string; type: 'SQL' | 'NOSQL' }>;
  @Input({ required: true }) dbGenerationOptions!: string[];
  @Input({ required: true }) showDbGeneration!: boolean;
  @Input({ required: true }) showPluralizeTableNames!: boolean;

  @Output() databaseTypeChange = new EventEmitter<'SQL' | 'NOSQL' | 'NONE'>();
  @Output() databaseSelectionChange = new EventEmitter<string>();

  onHelpIconInteraction(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
  }
}
