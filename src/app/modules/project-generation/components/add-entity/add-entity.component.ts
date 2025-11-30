import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface Field {
  type: string;
  name: string;
  maxLength?: number;
  primaryKey?: boolean;
  required?: boolean;
  unique?: boolean;
}

@Component({
  selector: 'app-add-entity',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './add-entity.component.html',
  styleUrls: ['./add-entity.component.css']
})
export class AddEntityComponent {
  @Output() save = new EventEmitter<any>();
  @Output() cancel = new EventEmitter<void>();

  entityName = '';
  mappedSuperclass = false;
  addRestEndpoints = false;
  nameError = '';

  fields: Field[] = [
    {
      type: 'Long',
      name: 'id',
      primaryKey: true,
      required: false,
      unique: false
    }
  ];

  fieldTypes = [
    'String',
    'Long',
    'Integer',
    'Double',
    'Float',
    'Boolean',
    'LocalDate',
    'LocalDateTime',
    'BigDecimal',
    'UUID',
    'Enum',
    'byte[]'
  ];

  addField(): void {
    this.fields.push({
      type: 'String',
      name: '',
      maxLength: 255,
      required: false,
      unique: false,
      primaryKey: false
    });
  }

  removeField(index: number): void {
    if (index > 0) {
      this.fields.splice(index, 1);
    }
  }

  onFieldTypeChange(field: Field): void {
    if (field.type === 'String') {
      if (!field.maxLength) {
        field.maxLength = 255;
      }
    } else {
      delete field.maxLength;
    }
  }

  validateEntityName(): boolean {
    if (!this.entityName.trim()) {
      this.nameError = 'Please provide a value.';
      return false;
    }
    this.nameError = '';
    return true;
  }

  onSave(): void {
    if (!this.validateEntityName()) {
      return;
    }

    const entity = {
      name: this.entityName,
      mappedSuperclass: this.mappedSuperclass,
      addRestEndpoints: this.addRestEndpoints,
      fields: this.fields
    };

    this.save.emit(entity);
  }

  onCancel(): void {
    this.cancel.emit();
  }
}
