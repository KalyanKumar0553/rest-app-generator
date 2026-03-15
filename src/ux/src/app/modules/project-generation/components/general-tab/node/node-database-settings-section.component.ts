import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';

@Component({
  selector: 'app-node-database-settings-section',
  standalone: true,
  imports: [CommonModule, FormsModule, MatExpansionModule, MatFormFieldModule, MatSelectModule],
  templateUrl: './node-database-settings-section.component.html',
  styleUrls: ['./general-tab-section.component.css']
})
export class NodeDatabaseSettingsSectionComponent {
  @Input({ required: true }) databaseSettings!: any;
  @Input({ required: true }) dbTypeOptions!: Array<'SQL' | 'NOSQL' | 'NONE'>;
  @Input({ required: true }) filteredDatabaseOptions!: Array<{ value: string; label: string; type: 'SQL' | 'NOSQL' }>;

  @Output() databaseTypeChange = new EventEmitter<'SQL' | 'NOSQL' | 'NONE'>();
  @Output() databaseSelectionChange = new EventEmitter<string>();
}
