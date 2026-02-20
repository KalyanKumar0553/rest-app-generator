import { Component, EventEmitter, Input, Output, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Field } from '../field-item/field-item.component';
import { SearchSortComponent, SearchConfig, SortOption, SearchSortEvent } from '../../../../components/search-sort/search-sort.component';
import { ConfirmationModalComponent, ModalButton } from '../../../../components/confirmation-modal/confirmation-modal.component';
import { FieldConfigComponent } from '../field-config/field-config.component';
import { ModalComponent } from '../../../../components/modal/modal.component';
import { ValidatorService } from '../../../../services/validator.service';
import { buildEntityNameRules, buildFieldListRules, buildFieldRules } from '../../validators/entity-validation';
import { FieldFilterService } from '../../../../services/field-filter.service';
import { SearchableMultiSelectComponent } from '../../../../components/searchable-multi-select/searchable-multi-select.component';

interface Entity {
  name: string;
  mappedSuperclass: boolean;
  addRestEndpoints: boolean;
  auditable?: boolean;
  softDelete?: boolean;
  immutable?: boolean;
  naturalIdCache?: boolean;
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
    MatIconModule,
    SearchSortComponent,
    SearchableMultiSelectComponent,
    ConfirmationModalComponent,
    FieldConfigComponent,
    ModalComponent
  ],
  templateUrl: './add-entity.component.html',
  styleUrls: ['./add-entity.component.css']
})
export class AddEntityComponent implements OnChanges {
  @Input() editEntity: Entity | null = null;
  @Input() isOpen = false;
  @Input() existingEntities: Entity[] = [];
  @Input() enumTypes: string[] = [];
  @Output() save = new EventEmitter<any>();
  @Output() cancel = new EventEmitter<void>();

  entityName = '';
  mappedSuperclass = false;
  addRestEndpoints = false;
  auditable = false;
  softDelete = false;
  immutable = false;
  naturalIdCache = false;
  selectedAdditionalConfigurations: string[] = [];
  nameError = '';

  fields: Field[] = [];

  private tempFields: Field[] = [];
  visibleFields: Array<{ field: Field; index: number }> = [];
  fieldSearchTerm = '';
  fieldSortOption: SortOption | null = null;
  isFieldConfigOpen = false;
  fieldConfigMode: 'add' | 'edit' = 'add';
  fieldConfigIndex: number | null = null;
  fieldDraft: Field | null = null;
  showFieldDeleteModal = false;
  fieldDeleteIndex: number | null = null;

  fieldDeleteModalConfig = {
    title: 'Delete field',
    message: ['Are you sure you want to delete this field?', 'This action cannot be undone.'],
    buttons: [
      { text: 'Cancel', type: 'cancel' as const, action: 'cancel' as const },
      { text: 'Delete', type: 'danger' as const, action: 'confirm' as const }
    ] as ModalButton[]
  };

  searchConfig: SearchConfig = {
    placeholder: 'Search fields by name, type, or constraint...',
    properties: ['name', 'type', 'constraints']
  };

  sortOptions: SortOption[] = [
    { label: 'Name (A-Z)', property: 'name', direction: 'asc' },
    { label: 'Name (Z-A)', property: 'name', direction: 'desc' },
    { label: 'Type (A-Z)', property: 'type', direction: 'asc' },
    { label: 'Type (Z-A)', property: 'type', direction: 'desc' },
    { label: 'Constraints (Most)', property: 'constraintCount', direction: 'desc' },
    { label: 'Constraints (Least)', property: 'constraintCount', direction: 'asc' },
    { label: 'Max Length (High)', property: 'maxLength', direction: 'desc' },
    { label: 'Max Length (Low)', property: 'maxLength', direction: 'asc' }
  ];

  private readonly baseFieldTypes = [
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
    'byte[]'
  ];

  get fieldTypes(): string[] {
    const enums = Array.isArray(this.enumTypes)
      ? this.enumTypes.map(item => String(item ?? '').trim()).filter(Boolean)
      : [];
    return Array.from(new Set([...this.baseFieldTypes, ...enums]));
  }

  additionalConfigurationOptions: string[] = [
    'Auditable',
    'Soft Delete',
    'Immutable',
    'Natural ID Cache'
  ];

  constructor(
    private validatorService: ValidatorService,
    private fieldFilterService: FieldFilterService
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['isOpen']) {
      if (this.isOpen) {
        if (this.editEntity) {
          this.loadEntityData(this.editEntity);
        } else {
          this.resetForm();
        }
      } else {
        this.tempFields = [];
      }
    }

    if (changes['editEntity'] && this.editEntity) {
      this.loadEntityData(this.editEntity);
    }

    if (changes['editEntity'] || changes['isOpen']) {
      this.updateVisibleFields();
    }
  }

  loadEntityData(entity: Entity): void {
    this.entityName = entity.name;
    this.mappedSuperclass = Boolean(entity.mappedSuperclass);
    this.addRestEndpoints = Boolean(entity.addRestEndpoints);
    this.normalizeExclusiveEntityToggles();
    this.auditable = Boolean(entity.auditable);
    this.softDelete = Boolean(entity.softDelete);
    this.immutable = Boolean(entity.immutable);
    this.naturalIdCache = Boolean(entity.naturalIdCache);
    this.syncAdditionalConfigurationsFromFlags();
    this.fields = JSON.parse(JSON.stringify(entity.fields));
    this.nameError = '';
    this.updateVisibleFields();
  }

  resetForm(): void {
    this.entityName = '';
    this.mappedSuperclass = false;
    this.addRestEndpoints = false;
    this.auditable = false;
    this.softDelete = false;
    this.immutable = false;
    this.naturalIdCache = false;
    this.selectedAdditionalConfigurations = [];
    this.nameError = '';
    this.fields = [];
    this.tempFields = [];
    this.updateVisibleFields();
  }

  addField(): void {
    this.fieldConfigMode = 'add';
    this.fieldConfigIndex = null;
    this.fieldDraft = {
      type: 'String',
      name: '',
      maxLength: 255,
      required: false,
      unique: false,
      primaryKey: false,
      softDelete: false,
      constraints: [],
      nameError: ''
    };
    this.isFieldConfigOpen = true;
    this.updateVisibleFields();
  }

  onFieldNameChange(field: Field): void {
    if (field.nameError) {
      field.nameError = '';
    }
    this.updateVisibleFields();
  }

  onEntityNameChange(): void {
    if (this.nameError) {
      this.nameError = '';
    }
  }

  removeField(index: number): void {
    if (index > 0) {
      this.fields.splice(index, 1);
      this.updateVisibleFields();
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
    this.updateVisibleFields();
  }

  validateEntityName(): boolean {
    const valid = this.validatorService.validate(
      this,
      buildEntityNameRules({
        entityName: this.entityName,
        existingEntities: this.existingEntities,
        editEntityName: this.editEntity?.name ?? null,
        setError: (message) => {
          this.nameError = message;
        }
      })
    );

    if (valid) {
      this.nameError = '';
    }
    return valid;
  }

  moveFieldUp(index: number): void {
    if (index > 1) {
      const temp = this.fields[index];
      this.fields[index] = this.fields[index - 1];
      this.fields[index - 1] = temp;
      this.updateVisibleFields();
    }
  }

  moveFieldDown(index: number): void {
    if (index < this.fields.length - 1 && index > 0) {
      const temp = this.fields[index];
      this.fields[index] = this.fields[index + 1];
      this.fields[index + 1] = temp;
      this.updateVisibleFields();
    }
  }

  onSave(): void {
    if (!this.validateEntityName()) {
      return;
    }

    if (!this.validatorService.validate(this, buildFieldListRules())) {
      return;
    }

    const entity = {
      name: this.entityName,
      mappedSuperclass: this.mappedSuperclass,
      addRestEndpoints: this.addRestEndpoints,
      auditable: this.auditable,
      softDelete: this.softDelete,
      immutable: this.immutable,
      naturalIdCache: this.naturalIdCache,
      fields: JSON.parse(JSON.stringify(this.fields))
    };

    this.save.emit(entity);
    this.tempFields = [];
    this.resetForm();
  }

  onCancel(): void {
    this.tempFields = [];
    this.resetForm();
    this.cancel.emit();
  }

  onMappedSuperclassChange(): void {
    if (this.mappedSuperclass) {
      this.addRestEndpoints = false;
      this.closeFieldConfig();
    }
    this.updateVisibleFields();
  }

  onAddRestEndpointsChange(): void {
    if (this.addRestEndpoints) {
      this.mappedSuperclass = false;
    }
    this.updateVisibleFields();
  }

  onAdditionalConfigurationsChange(values: string[]): void {
    this.selectedAdditionalConfigurations = Array.isArray(values) ? [...values] : [];
    const selectedSet = new Set(this.selectedAdditionalConfigurations);
    this.auditable = selectedSet.has('Auditable');
    this.softDelete = selectedSet.has('Soft Delete');
    this.immutable = selectedSet.has('Immutable');
    this.naturalIdCache = selectedSet.has('Natural ID Cache');
  }

  private syncAdditionalConfigurationsFromFlags(): void {
    const selected: string[] = [];
    if (this.auditable) {
      selected.push('Auditable');
    }
    if (this.softDelete) {
      selected.push('Soft Delete');
    }
    if (this.immutable) {
      selected.push('Immutable');
    }
    if (this.naturalIdCache) {
      selected.push('Natural ID Cache');
    }
    this.selectedAdditionalConfigurations = selected;
  }

  startEditField(index: number): void {
    this.fieldConfigMode = 'edit';
    this.fieldConfigIndex = index;
    this.fieldDraft = JSON.parse(JSON.stringify(this.fields[index]));
    if (this.fieldDraft && !this.fieldDraft.constraints) {
      this.fieldDraft.constraints = [];
    }
    this.isFieldConfigOpen = true;
  }

  onFieldConfigCancel(): void {
    this.closeFieldConfig();
  }

  onFieldConfigSave(field: Field): void {
    if (!this.validateFieldDraft(field)) {
      return;
    }

    this.applyConstraintMappings(field);
    if (field.primaryKey) {
      field.required = false;
    }

    if (this.fieldConfigMode === 'edit' && this.fieldConfigIndex !== null) {
      this.fields[this.fieldConfigIndex] = field;
    } else {
      this.fields.push(field);
    }

    if (field.primaryKey) {
      this.clearOtherPrimaryKeys(field);
    }

    this.closeFieldConfig();
    this.updateVisibleFields();
  }

  requestDeleteField(index: number): void {
    if (index <= 0) return;
    const fieldName = this.fields[index]?.name?.trim() || 'this field';
    this.fieldDeleteModalConfig.message = [
      `Are you sure you want to delete "${fieldName}"?`,
      'This will remove all constraints for the field.'
    ];
    this.fieldDeleteIndex = index;
    this.showFieldDeleteModal = true;
  }

  confirmDeleteField(): void {
    if (this.fieldDeleteIndex === null) {
      return;
    }
    this.removeField(this.fieldDeleteIndex);
    this.fieldDeleteIndex = null;
    this.showFieldDeleteModal = false;
  }

  cancelDeleteField(): void {
    this.fieldDeleteIndex = null;
    this.showFieldDeleteModal = false;
  }

  onFieldSearchSortChange(event: SearchSortEvent): void {
    this.fieldSearchTerm = event.searchTerm;
    this.fieldSortOption = event.sortOption;
    this.updateVisibleFields();
  }

  get hasActiveFilters(): boolean {
    return Boolean(this.fieldSearchTerm) || Boolean(this.fieldSortOption);
  }

  private updateVisibleFields(): void {
    this.visibleFields = this.fieldFilterService.getVisibleFields(
      this.fields,
      this.fieldSearchTerm,
      this.fieldSortOption
    );
  }

  getConstraintCount(field: Field): number {
    const listCount = field.constraints?.length ?? 0;
    if (listCount > 0) {
      return listCount;
    }
    return Number(Boolean(field.primaryKey)) + Number(Boolean(field.required)) + Number(Boolean(field.unique));
  }

  getConstraintLabel(field: Field): string {
    const count = this.getConstraintCount(field);
    return `${count} constraint${count === 1 ? '' : 's'}`;
  }

  get hasOtherPrimaryKey(): boolean {
    const draftIndex = this.fieldConfigIndex;
    return this.fields.some((existing, index) => {
      if (draftIndex !== null && index === draftIndex) {
        return false;
      }
      return Boolean(existing.primaryKey);
    });
  }

  private validateFieldDraft(field: Field): boolean {
    const duplicateField = this.fields.find((existingField, index) => {
      if (this.fieldConfigMode === 'edit' && this.fieldConfigIndex === index) {
        return false;
      }
      return existingField.name.toLowerCase() === field.name.trim().toLowerCase();
    });

    const valid = this.validatorService.validate(
      field,
      buildFieldRules({
        field,
        duplicateField: Boolean(duplicateField),
        hasOtherPrimaryKey: this.hasOtherPrimaryKey,
        setError: (message) => {
          field.nameError = message;
        }
      })
    );

    if (valid) {
      field.nameError = '';
      return true;
    }
    return false;
  }

  private closeFieldConfig(): void {
    this.isFieldConfigOpen = false;
    this.fieldConfigMode = 'add';
    this.fieldConfigIndex = null;
    this.fieldDraft = null;
  }

  private applyConstraintMappings(field: Field): void {
    const constraints = field.constraints ?? [];
    if (constraints.length === 0) {
      return;
    }

    for (const constraint of constraints) {
      const name = constraint.name.toLowerCase().trim();
      if (name === 'primary key' || name === 'primarykey' || name === 'primary_key') {
        field.primaryKey = true;
      }
      if (name === 'required' || name === 'not null' || name === 'not_null' || name === 'notnull') {
        field.required = true;
      }
      if (name === 'unique') {
        field.unique = true;
      }
      if (name === 'size' && field.type === 'String') {
        const parsed = Number(constraint.value2);
        if (!Number.isNaN(parsed)) {
          field.maxLength = parsed;
        }
      }
      if ((name === 'max length' || name === 'length') && constraint.value) {
        const parsed = Number(constraint.value);
        if (!Number.isNaN(parsed)) {
          field.maxLength = parsed;
        }
      }
    }
  }

  private clearOtherPrimaryKeys(selected: Field): void {
    for (const existing of this.fields) {
      if (existing === selected) {
        continue;
      }
      existing.primaryKey = false;
    }
  }

  private normalizeExclusiveEntityToggles(): void {
    if (this.mappedSuperclass && this.addRestEndpoints) {
      this.addRestEndpoints = false;
    }
  }
}
