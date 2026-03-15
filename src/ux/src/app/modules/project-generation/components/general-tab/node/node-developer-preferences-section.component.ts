import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxChange, MatCheckboxModule } from '@angular/material/checkbox';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatRadioModule } from '@angular/material/radio';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-node-developer-preferences-section',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatExpansionModule,
    MatIconModule,
    MatRadioModule,
    MatTooltipModule
  ],
  templateUrl: './node-developer-preferences-section.component.html',
  styleUrls: ['./general-tab-section.component.css']
})
export class NodeDeveloperPreferencesSectionComponent {
  @Input({ required: true }) developerPreferences!: any;

  @Output() addProfile = new EventEmitter<void>();
  @Output() removeProfile = new EventEmitter<string>();
  @Output() configureApiChange = new EventEmitter<MatCheckboxChange>();

  onHelpIconInteraction(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
  }
}
