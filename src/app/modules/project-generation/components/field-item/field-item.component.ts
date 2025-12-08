import { Component, Input, Output, EventEmitter, ViewChild, ElementRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgModel } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatAutocompleteModule } from '@angular/material/autocomplete';

export interface FieldValidation {
  key: string;
  searchTerm?: string;
  value?: string;
  secondValue?: string;
  inclusive?: boolean;
}

export interface Field {
  type: string;
  name: string;
  maxLength?: number;
  primaryKey?: boolean;
  nameError?: string;
  validations?: FieldValidation[];
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
    MatIconModule,
    MatAutocompleteModule
  ],
  templateUrl: './field-item.component.html',
  styleUrls: ['./field-item.component.css']
})
export class FieldItemComponent implements OnInit {
  @Input() field!: Field;
  @Input() index: number = 0;
  @Input() totalFields: number = 0;
  @Input() fieldTypes: string[] = [];
  @Input() canDelete: boolean = true;
  @Input() canMoveUp: boolean = false;
  @Input() canMoveDown: boolean = false;
  @Input() submitted: boolean = false;
  @Input() primaryLocked: boolean = false;
  @Input() readonlyName: boolean = false;

  @Output() fieldChange = new EventEmitter<Field>();
  @Output() fieldNameChange = new EventEmitter<Field>();
  @Output() fieldTypeChange = new EventEmitter<Field>();
  @Output() primaryChange = new EventEmitter<Field>();
  @Output() deleteField = new EventEmitter<void>();
  @Output() moveUp = new EventEmitter<void>();
  @Output() moveDown = new EventEmitter<void>();
  @ViewChild('fieldNameInput') fieldNameInput?: ElementRef<HTMLInputElement>;
  @ViewChild('fieldNameModel') fieldNameModel?: NgModel;

  validationOptions = [
    { value: 'naturalId', label: 'Natural ID' },
    { value: 'NotNull', label: 'Not Null' },
    { value: 'NotBlank', label: 'Not Blank' },
    { value: 'Email', label: 'Email' },
    { value: 'Unique', label: 'Unique' },
    { value: 'Nullable', label: 'Allow Null' },
    { value: 'Length', label: 'Length' },
    { value: 'Min', label: 'Minimum Value' },
    { value: 'Max', label: 'Maximum Value' },
    { value: 'Past', label: 'Should be past' },
    { value: 'PastOrPresent', label: 'Should be past or Present' },
    { value: 'AssertTrue', label: 'Always True' },
    { value: 'AssertFalse', label: 'Always False' },
    { value: 'Pattern', label: 'Pattern' },
    { value: 'Positive', label: 'Only Positive Values' },
    { value: 'DecimalMin', label: 'Decimal Min' },
    { value: 'Digits', label: 'Digits (Fractional Values)' },
    { value: 'Future', label: 'Should Be Future' }
  ];

  ngOnInit(): void {
    if (!this.field.validations || this.field.validations.length === 0) {
      this.field.validations = [{ key: '', searchTerm: '' }];
    }
    this.syncValidationLabels();
  }

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

  addValidationRow(afterIndex?: number): void {
    if (!this.field.validations) {
      this.field.validations = [];
    }
    const insertIndex = afterIndex !== undefined ? afterIndex + 1 : this.field.validations.length;
    this.field.validations.splice(insertIndex, 0, { key: '', searchTerm: '' });
    this.onFieldUpdate();
  }

  removeValidationRow(index: number): void {
    if (!this.field.validations) {
      return;
    }
    if (this.field.validations.length === 1) {
      this.field.validations[0].key = '';
    } else {
      this.field.validations.splice(index, 1);
    }
    this.onFieldUpdate();
  }

  onValidationChange(index: number, value: string): void {
    if (!this.field.validations) {
      this.field.validations = [];
    }
    this.field.validations[index].key = value;
    this.field.validations[index].searchTerm = this.getValidationLabel(value);
    this.resetValidationValues(index, value);
    this.onFieldUpdate();
  }

  onValidationInput(index: number, value: string): void {
    if (!this.field.validations) {
      return;
    }
    this.field.validations[index].searchTerm = value;
  }

  getAvailableValidationOptions(index: number) {
    const selectedKeys = (this.field.validations || [])
      .map((v, idx) => (idx === index ? null : v.key))
      .filter((k): k is string => !!k);
    const search = (this.field.validations?.[index]?.searchTerm || '').toLowerCase();
    return this.validationOptions.filter(
      option =>
        !selectedKeys.includes(option.value) &&
        (!search || option.label.toLowerCase().includes(search) || option.value.toLowerCase().includes(search))
    );
  }

  onValidationSelected(index: number, value: string): void {
    this.onValidationChange(index, value);
    this.field.validations![index].searchTerm = this.getValidationLabel(value);
  }

  private getValidationLabel(value: string): string {
    return this.validationOptions.find(o => o.value === value)?.label || value;
  }

  private syncValidationLabels(): void {
    if (!this.field.validations) return;
    this.field.validations = this.field.validations.map(v => ({
      key: v.key || '',
      searchTerm: v.searchTerm ?? (v.key ? this.getValidationLabel(v.key) : ''),
      value: v.value,
      secondValue: v.secondValue,
      inclusive: v.inclusive
    }));
  }

  private resetValidationValues(index: number, key: string): void {
    if (!this.field.validations) {
      return;
    }
    const validation = this.field.validations[index];
    validation.value = undefined;
    validation.secondValue = undefined;
    validation.inclusive = false;
    if (key === 'Digits') {
      validation.value = '2';
      validation.secondValue = '2';
    }
  }

  focusNameInput(): void {
    this.fieldNameInput?.nativeElement.focus();
    this.fieldNameInput?.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }

  onPrimaryChange(): void {
    this.primaryChange.emit(this.field);
    this.fieldChange.emit(this.field);
  }

  setCustomError(message: string | null): void {
    if (!this.fieldNameModel?.control) {
      return;
    }
    const currentErrors = { ...(this.fieldNameModel.control.errors || {}) };
    if (message) {
      currentErrors['custom'] = true;
    } else {
      delete currentErrors['custom'];
    }
    const finalErrors = Object.keys(currentErrors).length ? currentErrors : null;
    this.fieldNameModel.control.setErrors(finalErrors);
    this.fieldNameModel.control.updateValueAndValidity();
  }

  markAsTouched(): void {
    this.fieldNameModel?.control.markAsTouched();
    this.fieldNameModel?.control.updateValueAndValidity();
  }
}
