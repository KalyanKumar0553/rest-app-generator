import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxChange, MatCheckboxModule } from '@angular/material/checkbox';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-java-developer-preferences-section',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatIconModule,
    MatRadioModule,
    MatSelectModule,
    MatTooltipModule
  ],
  templateUrl: './java-developer-preferences-section.component.html',
  styleUrls: ['./general-tab-section.component.css']
})
export class JavaDeveloperPreferencesSectionComponent {
  @Input({ required: true }) developerPreferences!: any;
  @Input({ required: true }) javaVersionOptions!: string[];

  @Output() addProfile = new EventEmitter<void>();
  @Output() removeProfile = new EventEmitter<string>();
  @Output() enableActuatorChange = new EventEmitter<boolean>();
  @Output() configureApiChange = new EventEmitter<MatCheckboxChange>();

  onHelpIconInteraction(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
  }
}
