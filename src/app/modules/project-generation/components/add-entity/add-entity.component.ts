import { Component, EventEmitter, Input, Output, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

interface Field {
  type: string;
  name: string;
  maxLength?: number;
  primaryKey?: boolean;
  required?: boolean;
  unique?: boolean;
  nameError?: string;
}

interface Entity {
  name: string;
  mappedSuperclass: boolean;
  addRestEndpoints: boolean;
  fields: Field[];
}

@Component({
  selector: 'app-add-entity',
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
  templateUrl: './add-entity.component.html',
  styleUrls: ['./add-entity.component.css']
})
export class AddEntityComponent implements OnChanges {
  @Input() editEntity: Entity | null = null;
  @Input() isOpen = false;
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

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['isOpen']) {
      if (this.isOpen) {
        if (this.editEntity) {
          this.loadEntityData(this.editEntity);
        } else {
          this.resetForm();
        }
      }
    }

    if (changes['editEntity'] && this.editEntity) {
      this.loadEntityData(this.editEntity);
    }
  }

  loadEntityData(entity: Entity): void {
    this.entityName = entity.name;
    this.mappedSuperclass = entity.mappedSuperclass;
    this.addRestEndpoints = entity.addRestEndpoints;
    this.fields = JSON.parse(JSON.stringify(entity.fields));
    this.nameError = '';
  }

  resetForm(): void {
    this.entityName = '';
    this.mappedSuperclass = false;
    this.addRestEndpoints = false;
    this.nameError = '';
    this.fields = [
      {
        type: 'Long',
        name: 'id',
        primaryKey: true,
        required: false,
        unique: false
      }
    ];
  }

  addField(): void {
    this.fields.push({
      type: 'String',
      name: '',
      maxLength: 255,
      required: false,
      unique: false,
      primaryKey: false,
      nameError: ''
    });
  }

  onFieldNameChange(field: Field): void {
    if (field.nameError) {
      field.nameError = '';
    }
  }

  onEntityNameChange(): void {
    if (this.nameError) {
      this.nameError = '';
    }
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
      this.nameError = 'Entity name is required.';
      return false;
    }
    this.nameError = '';
    return true;
  }

  validateFieldName(field: Field): boolean {
    if (!field.name.trim()) {
      field.nameError = 'Field name is required.';
      return false;
    }

    const alphanumericPattern = /^[a-zA-Z0-9]+$/;
    if (!alphanumericPattern.test(field.name)) {
      field.nameError = 'Field name must be alphanumeric without spaces.';
      return false;
    }

    field.nameError = '';
    return true;
  }

  validateAllFields(): boolean {
    let isValid = true;
    for (const field of this.fields) {
      if (!field.primaryKey && !this.validateFieldName(field)) {
        isValid = false;
      }
    }
    return isValid;
  }

  moveFieldUp(index: number): void {
    if (index > 1) {
      const temp = this.fields[index];
      this.fields[index] = this.fields[index - 1];
      this.fields[index - 1] = temp;
    }
  }

  moveFieldDown(index: number): void {
    if (index < this.fields.length - 1 && index > 0) {
      const temp = this.fields[index];
      this.fields[index] = this.fields[index + 1];
      this.fields[index + 1] = temp;
    }
  }

  onSave(): void {
    if (!this.validateEntityName()) {
      return;
    }

    if (!this.validateAllFields()) {
      return;
    }

    const entity = {
      name: this.entityName,
      mappedSuperclass: this.mappedSuperclass,
      addRestEndpoints: this.addRestEndpoints,
      fields: JSON.parse(JSON.stringify(this.fields))
    };

    this.save.emit(entity);
    this.resetForm();
  }

  onCancel(): void {
    this.resetForm();
    this.cancel.emit();
  }

  onMappedSuperclassChange(): void {
    if (this.mappedSuperclass) {
      this.fields = [];
      this.addRestEndpoints = false;
    } else {
      this.fields = [
        {
          type: 'Long',
          name: 'id',
          primaryKey: true,
          required: false,
          unique: false
        }
      ];
    }
  }
}
