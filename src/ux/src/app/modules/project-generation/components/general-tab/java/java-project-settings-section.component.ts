import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { APP_SETTINGS } from '../../../../../settings/app-settings';

@Component({
  selector: 'app-java-project-settings-section',
  standalone: true,
  imports: [CommonModule, FormsModule, MatExpansionModule, MatFormFieldModule, MatInputModule, MatRadioModule],
  templateUrl: './java-project-settings-section.component.html',
  styleUrls: ['./general-tab-section.component.css']
})
export class JavaProjectSettingsSectionComponent {
  @Input({ required: true }) projectSettings!: any;
  @Input() projectGroupError = '';
  @Input() projectNameError = '';

  @Output() projectGroupChange = new EventEmitter<void>();
  @Output() projectNameChange = new EventEmitter<void>();

  readonly appSettings = APP_SETTINGS;
}
