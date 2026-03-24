import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { APP_SETTINGS } from '../../../../../settings/app-settings';

@Component({
  selector: 'app-node-project-settings-section',
  standalone: true,
  imports: [CommonModule, FormsModule, MatExpansionModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  templateUrl: './node-project-settings-section.component.html',
  styleUrls: ['./general-tab-section.component.css']
})
export class NodeProjectSettingsSectionComponent {
  @Input() runtimeLabel = 'Node';
  @Input() showPackageManager = true;
  @Input({ required: true }) projectSettings!: any;
  @Input() projectGroupError = '';
  @Input() projectNameError = '';
  @Input() projectDescriptionError = '';
  @Input({ required: true }) packageManagerOptions!: string[];

  @Output() projectGroupChange = new EventEmitter<void>();
  @Output() projectNameChange = new EventEmitter<void>();
  @Output() projectDescriptionChange = new EventEmitter<void>();

  readonly appSettings = APP_SETTINGS;
}
