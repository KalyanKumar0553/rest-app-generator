import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface Field {
  type: string;
  name: string;
  jsonProperty?: string;
  maxLength?: number;
  primaryKey?: boolean;
  required?: boolean;
  unique?: boolean;
  softDelete?: boolean;
  constraints?: Array<{ name: string; value?: string; value2?: string }>;
  nameError?: string;
}

@Component({
  selector: 'app-field-item',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './field-item.component.html',
  styleUrls: ['./field-item.component.css']
})
export class FieldItemComponent {
  @Input() field!: Field;
  @Input() index: number = 0;
  @Input() totalFields: number = 0;
  @Input() fieldTypes: string[] = [];
  @Input() canDelete: boolean = true;
  @Input() canMoveUp: boolean = false;
  @Input() canMoveDown: boolean = false;

  @Output() fieldChange = new EventEmitter<Field>();
  @Output() fieldNameChange = new EventEmitter<Field>();
  @Output() fieldTypeChange = new EventEmitter<Field>();
  @Output() deleteField = new EventEmitter<void>();
  @Output() moveUp = new EventEmitter<void>();
  @Output() moveDown = new EventEmitter<void>();

  onNameChange(): void {
    this.fieldNameChange.emit(this.field);
    this.fieldChange.emit(this.field);
  }

  onTypeChange(): void {
    this.fieldTypeChange.emit(this.field);
    this.fieldChange.emit(this.field);
  }

  onDelete(): void {
    this.deleteField.emit();
  }

  onMoveUp(): void {
    this.moveUp.emit();
  }

  onMoveDown(): void {
    this.moveDown.emit();
  }

  onFieldUpdate(): void {
    this.fieldChange.emit(this.field);
  }
}
