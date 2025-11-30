import { Component, EventEmitter, Output, Input } from '@angular/core';
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
export class AddEntityComponent {
  @Output() save = new EventEmitter<Entity>();
  @Output() cancel = new EventEmitter<void>();
  @Input() editEntity?: Entity;

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

  ngOnInit(): void {
    if (this.editEntity) {
      this.entityName = this.editEntity.name;
      this.mappedSuperclass = this.editEntity.mappedSuperclass;
      this.addRestEndpoints = this.editEntity.addRestEndpoints;
      this.fields = [...this.editEntity.fields];
    }
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

    const entity: Entity = {
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
