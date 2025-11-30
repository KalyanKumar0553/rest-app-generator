import { Component, EventEmitter, Input, Output, OnChanges, SimpleChanges } from '@angular/core';
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

interface Entity {
  name: string;
  mappedSuperclass: boolean;
  addRestEndpoints: boolean;
  fields: Field[];
}

@Component({
  selector: 'app-add-entity',
  standalone: true,
  imports: [CommonModule, FormsModule],
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
      fields: JSON.parse(JSON.stringify(this.fields))
    };

    this.save.emit(entity);
    this.resetForm();
  }

  onCancel(): void {
    this.resetForm();
    this.cancel.emit();
  }
}
