import { Component, EventEmitter, Input, Output, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { Field } from '../field-item/field-item.component';

@Component({
  selector: 'app-field-config',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatSelectModule,
    MatIconModule
  ],
  templateUrl: './field-config.component.html',
  styleUrls: ['./field-config.component.css']
})
export class FieldConfigComponent implements OnChanges {
  @Input() field!: Field;
  @Input() fieldTypes: string[] = [];
  @Input() mode: 'add' | 'edit' = 'add';
  @Input() hasOtherPrimaryKey = false;
  @Output() save = new EventEmitter<Field>();
  @Output() cancel = new EventEmitter<void>();

  constraintError = '';
  constraintOptions = [
    'Primary Key',
    'Required',
    'Unique',
    'Min',
    'Max',
    'Max Length',
    'Pattern',
    'Default'
  ];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['field'] || changes['mode']) {
      this.sortConstraintsForEdit();
    }
  }

  onFieldNameChange(): void {
    if (this.field.nameError) {
      this.field.nameError = '';
    }
  }

  onFieldTypeChange(): void {
    if (this.field.type === 'String') {
      if (!this.field.maxLength) {
        this.field.maxLength = 255;
      }
    } else {
      delete this.field.maxLength;
    }
  }

  addConstraint(): void {
    if (!this.field.constraints) {
      this.field.constraints = [];
    }
    this.field.constraints.push({ name: '', value: '' });
  }

  removeConstraint(index: number): void {
    this.field.constraints?.splice(index, 1);
  }

  onSave(): void {
    this.constraintError = '';
    const constraints = this.field.constraints ?? [];
    const invalidConstraint = constraints.find(
      constraint => !constraint.name?.trim() || !constraint.value?.trim()
    );
    if (invalidConstraint) {
      this.constraintError = 'Constraint name and value are required.';
      return;
    }
    this.save.emit(this.field);
  }

  onCancel(): void {
    this.cancel.emit();
  }

  private sortConstraintsForEdit(): void {
    if (this.mode !== 'edit') {
      return;
    }
    if (!this.field || !this.field.constraints || this.field.constraints.length === 0) {
      return;
    }
    this.field.constraints.sort((a, b) => {
      const nameA = (a.name || '').toLowerCase();
      const nameB = (b.name || '').toLowerCase();
      if (!nameA && !nameB) return 0;
      if (!nameA) return 1;
      if (!nameB) return -1;
      return nameA.localeCompare(nameB);
    });
  }
}
