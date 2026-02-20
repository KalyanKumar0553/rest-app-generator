import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { Field } from '../field-item/field-item.component';
import {
  getConstraintValidationError,
  getConstraintDefinition,
  getConstraintInputConfig,
  getConstraintOptionsForFieldType,
  getConstraintValueMode,
  normalizeConstraintValuesForMode
} from '../../constants/field-constraints';
import { ToastService } from '../../../../services/toast.service';

@Component({
  selector: 'app-edit-property',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatIconModule
  ],
  templateUrl: './edit-property.component.html',
  styleUrls: ['./edit-property.component.css']
})
export class EditPropertyComponent implements OnChanges {
  @Input() field!: Field;
  @Input() fieldTypes: string[] = [];
  @Input() mode: 'add' | 'edit' = 'add';
  @Output() save = new EventEmitter<Field>();
  @Output() cancel = new EventEmitter<void>();

  constraintError = '';

  constructor(private toastService: ToastService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['field'] || changes['mode']) {
      this.pruneConstraintsForFieldType();
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
    this.pruneConstraintsForFieldType();
  }

  addConstraint(): void {
    if (!this.field.constraints) {
      this.field.constraints = [];
    }
    this.field.constraints.push({ name: '', value: '', value2: '' });
  }

  removeConstraint(index: number): void {
    this.field.constraints?.splice(index, 1);
  }

  onSave(): void {
    this.constraintError = '';
    const error = getConstraintValidationError(this.field.constraints);
    if (error) {
      this.constraintError = error;
      this.toastService.error(this.constraintError);
      return;
    }

    this.field.primaryKey = false;
    this.field.required = false;
    this.field.softDelete = false;
    this.field.jsonProperty = this.field.jsonProperty?.trim() || undefined;
    this.save.emit(this.field);
  }

  onCancel(): void {
    this.cancel.emit();
  }

  onConstraintNameChange(index: number): void {
    this.constraintError = '';
    const constraint = this.field.constraints?.[index];
    if (!constraint) {
      return;
    }
    normalizeConstraintValuesForMode(constraint);
  }

  getAvailableConstraintOptions(index: number): string[] {
    const typeOptions = getConstraintOptionsForFieldType(this.field?.type ?? '', this.fieldTypes);
    const selectedInOtherRows = new Set(
      (this.field.constraints ?? [])
        .map((constraint, i) => (i === index ? '' : constraint.name?.trim()))
        .filter(Boolean) as string[]
    );

    const filtered = typeOptions.filter(option => !selectedInOtherRows.has(option));
    const currentName = this.field.constraints?.[index]?.name?.trim();
    if (currentName && !filtered.includes(currentName)) {
      filtered.push(currentName);
    }

    return filtered.sort((a, b) => a.localeCompare(b));
  }

  hasFirstValue(constraintName: string | undefined): boolean {
    return getConstraintValueMode(constraintName) !== 'none';
  }

  hasSecondValue(constraintName: string | undefined): boolean {
    return getConstraintValueMode(constraintName) === 'double';
  }

  getFirstInputLabel(constraintName: string | undefined): string {
    return getConstraintInputConfig(constraintName, 1)?.label ?? 'Value';
  }

  getFirstInputPlaceholder(constraintName: string | undefined): string {
    return getConstraintInputConfig(constraintName, 1)?.placeholder ?? 'Enter value';
  }

  getSecondInputLabel(constraintName: string | undefined): string {
    return getConstraintInputConfig(constraintName, 2)?.label ?? 'Value 2';
  }

  getSecondInputPlaceholder(constraintName: string | undefined): string {
    return getConstraintInputConfig(constraintName, 2)?.placeholder ?? 'Enter value';
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
      if (!nameA && !nameB) {
        return 0;
      }
      if (!nameA) {
        return 1;
      }
      if (!nameB) {
        return -1;
      }
      return nameA.localeCompare(nameB);
    });
  }

  private pruneConstraintsForFieldType(): void {
    if (!this.field) {
      return;
    }
    const constraints = this.field.constraints ?? [];
    if (constraints.length === 0) {
      return;
    }

    const allowedOptions = new Set(getConstraintOptionsForFieldType(this.field.type, this.fieldTypes));
    this.field.constraints = constraints.filter(constraint => {
      if (!constraint.name?.trim()) {
        return true;
      }
      const name = constraint.name.trim();
      const definition = getConstraintDefinition(name);
      if (!definition) {
        return true;
      }
      return allowedOptions.has(name);
    });
  }
}
