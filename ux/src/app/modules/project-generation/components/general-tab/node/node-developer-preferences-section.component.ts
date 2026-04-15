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
  selector: 'app-node-developer-preferences-section',
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
  templateUrl: './node-developer-preferences-section.component.html',
  styleUrls: ['./general-tab-section.component.css']
})
export class NodeDeveloperPreferencesSectionComponent {
  @Input() runtimeLabel = 'Node';
  @Input() apiFrameworkLabel = 'Express';
  @Input({ required: true }) developerPreferences!: any;

  private static readonly PYTHON_ORM_OPTIONS: ReadonlyArray<{ value: string; label: string }> = [
    { value: 'sqlalchemy', label: 'SQLAlchemy' },
    { value: 'django', label: 'Django ORM' }
  ];
  private static readonly NODE_ORM_OPTIONS: ReadonlyArray<{ value: string; label: string }> = [
    { value: 'prisma', label: 'Prisma' },
    { value: 'sequelize', label: 'Sequelize' }
  ];

  get ormOptions(): ReadonlyArray<{ value: string; label: string }> {
    return this.runtimeLabel === 'Python'
      ? NodeDeveloperPreferencesSectionComponent.PYTHON_ORM_OPTIONS
      : NodeDeveloperPreferencesSectionComponent.NODE_ORM_OPTIONS;
  }

  @Output() addProfile = new EventEmitter<void>();
  @Output() removeProfile = new EventEmitter<string>();
  @Output() configureApiChange = new EventEmitter<MatCheckboxChange>();

  onHelpIconInteraction(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
  }
}
