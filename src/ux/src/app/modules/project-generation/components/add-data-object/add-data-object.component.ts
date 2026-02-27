import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatRadioModule } from '@angular/material/radio';
import { Field } from '../field-item/field-item.component';
import { SearchSortComponent, SearchConfig, SortOption, SearchSortEvent } from '../../../../components/search-sort/search-sort.component';
import { ConfirmationModalComponent, ModalButton } from '../../../../components/confirmation-modal/confirmation-modal.component';
import { EditPropertyComponent } from '../edit-property/edit-property.component';
import { ModalComponent } from '../../../../components/modal/modal.component';
import { ValidatorService } from '../../../../services/validator.service';
import { buildEntityNameRules, buildFieldListRules, buildFieldRules } from '../../validators/entity-validation';
import { FieldFilterService } from '../../../../services/field-filter.service';
import { HelpPopoverComponent } from '../../../../components/help-popover/help-popover.component';
import { SearchableMultiSelectComponent } from '../../../../components/searchable-multi-select/searchable-multi-select.component';
import { DTO_FIELD_TYPE_OPTIONS } from '../../constants/backend-field-types';
import { VALIDATION_MESSAGES } from '../../constants/validation-messages';

interface DataObject {
  name: string;
  dtoType?: 'request' | 'response';
  classMethods?: {
    toString: boolean;
    hashCode: boolean;
    equals: boolean;
    noArgsConstructor: boolean;
    allArgsConstructor: boolean;
    builder: boolean;
  };
  responseWrapper?: 'STANDARD_ENVELOPE' | 'NONE' | 'UPSERT';
  enableFieldProjection?: boolean;
  includeHateoasLinks?: boolean;
  fields: Field[];
}

@Component({
  selector: 'app-add-data-object',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatRadioModule,
    MatButtonModule,
    MatIconModule,
    SearchSortComponent,
    SearchableMultiSelectComponent,
    HelpPopoverComponent,
    ConfirmationModalComponent,
    EditPropertyComponent,
    ModalComponent
  ],
  templateUrl: './add-data-object.component.html',
  styleUrls: ['./add-data-object.component.css']
})
export class AddDataObjectComponent implements OnChanges {
  @Input() editDataObject: DataObject | null = null;
  @Input() isOpen = false;
  @Input() existingDataObjects: DataObject[] = [];
  @Input() availableModels: Array<{ name: string }> = [];
  @Input() enumTypes: string[] = [];
  @Output() save = new EventEmitter<DataObject>();
  @Output() cancel = new EventEmitter<void>();

  dataObjectName = '';
  dtoType: 'request' | 'response' = 'request';
  classMethods = this.getDefaultClassMethods();
  selectedClassMethods: string[] = [];
  responseWrapper: 'STANDARD_ENVELOPE' | 'NONE' | 'UPSERT' = 'STANDARD_ENVELOPE';
  enableFieldProjection = true;
  includeHateoasLinks = true;
  nameError = '';
  propertiesError = '';

  fields: Field[] = [];

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
    title: 'Delete property',
    message: ['Are you sure you want to delete this property?', 'This action cannot be undone.'],
    buttons: [
      { text: 'Cancel', type: 'cancel' as const, action: 'cancel' as const },
      { text: 'Delete', type: 'danger' as const, action: 'confirm' as const }
    ] as ModalButton[]
  };

  searchConfig: SearchConfig = {
    placeholder: 'Search properties by name, type, json property, or constraint...',
    properties: ['name', 'type', 'jsonProperty', 'constraints']
  };

  sortOptions: SortOption[] = [
    { label: 'Name (A-Z)', property: 'name', direction: 'asc' },
    { label: 'Name (Z-A)', property: 'name', direction: 'desc' },
    { label: 'Type (A-Z)', property: 'type', direction: 'asc' },
    { label: 'Type (Z-A)', property: 'type', direction: 'desc' },
    { label: 'JSON Property (A-Z)', property: 'jsonProperty', direction: 'asc' },
    { label: 'JSON Property (Z-A)', property: 'jsonProperty', direction: 'desc' },
    { label: 'Constraints (Most)', property: 'constraintCount', direction: 'desc' },
    { label: 'Constraints (Least)', property: 'constraintCount', direction: 'asc' },
    { label: 'Max Length (High)', property: 'maxLength', direction: 'desc' },
    { label: 'Max Length (Low)', property: 'maxLength', direction: 'asc' }
  ];

  private readonly baseFieldTypes = DTO_FIELD_TYPE_OPTIONS;
  classMethodOptions: string[] = [
    'toString',
    'hashcode',
    'equals',
    'NoArgsConstructor',
    'AllArgsConstructor',
    'Builder'
  ];

  get fieldTypes(): string[] {
    const enums = Array.isArray(this.enumTypes)
      ? this.enumTypes.map(item => String(item ?? '').trim()).filter(Boolean)
      : [];
    const dtoNames = (Array.isArray(this.existingDataObjects) ? this.existingDataObjects : [])
      .map(item => String(item?.name ?? '').trim())
      .filter(Boolean);
    const listDtoNames = dtoNames.map((name) => `List<${name}>`);
    return Array.from(new Set([...this.baseFieldTypes, ...enums, ...dtoNames, ...listDtoNames]));
  }

  constructor(
    private validatorService: ValidatorService,
    private fieldFilterService: FieldFilterService
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['isOpen']) {
      if (this.isOpen) {
        if (this.editDataObject) {
          this.loadDataObject(this.editDataObject);
        } else {
          this.resetForm();
        }
      }
    }

    if (changes['editDataObject'] && this.editDataObject) {
      this.loadDataObject(this.editDataObject);
    }

    if (changes['editDataObject'] || changes['isOpen']) {
      this.updateVisibleFields();
    }

  }

  loadDataObject(dataObject: DataObject): void {
    this.dataObjectName = dataObject.name;
    this.dtoType = dataObject.dtoType ?? 'request';
    this.classMethods = this.parseClassMethods(dataObject.classMethods);
    this.syncClassMethodsSelectionFromFlags();
    this.responseWrapper = dataObject.responseWrapper ?? 'STANDARD_ENVELOPE';
    this.enableFieldProjection = Boolean(dataObject.enableFieldProjection ?? true);
    this.includeHateoasLinks = Boolean(dataObject.includeHateoasLinks ?? true);
    this.fields = this.sanitizeProperties(JSON.parse(JSON.stringify(dataObject.fields)));
    this.nameError = '';
    this.propertiesError = '';
    this.updateVisibleFields();
  }

  resetForm(): void {
    this.dataObjectName = '';
    this.dtoType = 'request';
    this.classMethods = this.getDefaultClassMethods();
    this.syncClassMethodsSelectionFromFlags();
    this.responseWrapper = 'STANDARD_ENVELOPE';
    this.enableFieldProjection = true;
    this.includeHateoasLinks = true;
    this.nameError = '';
    this.propertiesError = '';
    this.fields = [];
    this.updateVisibleFields();
  }

  addField(): void {
    this.propertiesError = '';
    this.fieldConfigMode = 'add';
    this.fieldConfigIndex = null;
    this.fieldDraft = {
      type: 'String',
      name: '',
      maxLength: 255,
      jsonProperty: '',
      constraints: [],
      nameError: ''
    };
    this.isFieldConfigOpen = true;
    this.updateVisibleFields();
  }

  onDataObjectNameChange(): void {
    if (this.nameError) {
      this.nameError = '';
    }
  }

  onDtoTypeChange(): void {
    this.responseWrapper = 'STANDARD_ENVELOPE';
    this.enableFieldProjection = true;
    this.includeHateoasLinks = true;
  }

  onClassMethodsChange(values: string[]): void {
    this.selectedClassMethods = Array.isArray(values) ? [...values] : [];
    const selectedSet = new Set(this.selectedClassMethods);
    this.classMethods = {
      toString: selectedSet.has('toString'),
      hashCode: selectedSet.has('hashcode'),
      equals: selectedSet.has('equals'),
      noArgsConstructor: selectedSet.has('NoArgsConstructor'),
      allArgsConstructor: selectedSet.has('AllArgsConstructor'),
      builder: selectedSet.has('Builder')
    };
  }

  removeField(index: number): void {
    this.fields.splice(index, 1);
    this.updateVisibleFields();
  }

  validateDataObjectName(): boolean {
    const valid = this.validatorService.validate(
      this,
      buildEntityNameRules({
        entityName: this.dataObjectName,
        existingEntities: this.existingDataObjects,
        editEntityName: this.editDataObject?.name ?? null,
        fieldName: 'dataObjectName',
        label: 'Data object',
        setError: (message) => {
          this.nameError = message;
        }
      }),
      { silent: true }
    );

    if (valid) {
      this.nameError = '';
    }
    return valid;
  }

  onSave(): void {
    if (!this.validateDataObjectName()) {
      this.focusFirstErrorField();
      return;
    }

    if (!this.fields?.length) {
      this.propertiesError = VALIDATION_MESSAGES.atLeastOneProperty;
      this.focusFirstErrorField();
      return;
    }
    this.propertiesError = '';

    if (!this.validatorService.validate(this, buildFieldListRules({
      requirePrimaryKey: false,
      itemLabel: 'property'
    }), { silent: true })) {
      this.propertiesError = VALIDATION_MESSAGES.atLeastOneProperty;
      this.focusFirstErrorField();
      return;
    }

    this.save.emit({
      name: this.dataObjectName,
      dtoType: this.dtoType,
      classMethods: { ...this.classMethods },
      responseWrapper: this.dtoType === 'response' ? this.responseWrapper : undefined,
      enableFieldProjection: this.dtoType === 'response' ? this.enableFieldProjection : undefined,
      includeHateoasLinks: this.dtoType === 'response' ? this.includeHateoasLinks : undefined,
      fields: this.sanitizeProperties(JSON.parse(JSON.stringify(this.fields)))
    });

    this.resetForm();
  }

  onCancel(): void {
    this.resetForm();
    this.cancel.emit();
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

    const sanitized = this.sanitizeProperty(field);
    this.applyConstraintMappings(sanitized);

    if (this.fieldConfigMode === 'edit' && this.fieldConfigIndex !== null) {
      this.fields[this.fieldConfigIndex] = sanitized;
    } else {
      this.fields.push(sanitized);
    }

    this.closeFieldConfig();
    this.updateVisibleFields();
  }

  requestDeleteField(index: number): void {
    const fieldName = this.fields[index]?.name?.trim() || 'this property';
    this.fieldDeleteModalConfig.message = [
      `Are you sure you want to delete "${fieldName}"?`,
      'This will remove all constraints for the property.'
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

  getConstraintCount(field: Field): number {
    return field.constraints?.length ?? 0;
  }

  getConstraintLabel(field: Field): string {
    const count = this.getConstraintCount(field);
    return `${count} constraint${count === 1 ? '' : 's'}`;
  }

  private updateVisibleFields(): void {
    this.visibleFields = this.fieldFilterService.getVisibleFields(
      this.fields,
      this.fieldSearchTerm,
      this.fieldSortOption
    );
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
        hasOtherPrimaryKey: false,
        enforcePrimaryKey: false,
        setError: (message) => {
          field.nameError = message;
        }
      }),
      { silent: true }
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

  private sanitizeProperties(fields: Field[]): Field[] {
    return (fields ?? []).map(field => this.sanitizeProperty(field));
  }

  private sanitizeProperty(field: Field): Field {
    return {
      ...field,
      jsonProperty: field.jsonProperty?.trim() || undefined,
      primaryKey: false,
      required: false,
      softDelete: false
    };
  }

  private getDefaultClassMethods(): {
    toString: boolean;
    hashCode: boolean;
    equals: boolean;
    noArgsConstructor: boolean;
    allArgsConstructor: boolean;
    builder: boolean;
  } {
    return {
      toString: true,
      hashCode: true,
      equals: true,
      noArgsConstructor: true,
      allArgsConstructor: true,
      builder: false
    };
  }

  private parseClassMethods(raw: unknown): {
    toString: boolean;
    hashCode: boolean;
    equals: boolean;
    noArgsConstructor: boolean;
    allArgsConstructor: boolean;
    builder: boolean;
  } {
    const fallback = this.getDefaultClassMethods();
    if (!raw || typeof raw !== 'object') {
      return fallback;
    }
    const value = raw as Record<string, unknown>;
    return {
      toString: Boolean(value['toString'] ?? fallback.toString),
      hashCode: Boolean(value['hashCode'] ?? fallback.hashCode),
      equals: Boolean(value['equals'] ?? fallback.equals),
      noArgsConstructor: Boolean(value['noArgsConstructor'] ?? fallback.noArgsConstructor),
      allArgsConstructor: Boolean(value['allArgsConstructor'] ?? fallback.allArgsConstructor),
      builder: Boolean(value['builder'] ?? fallback.builder)
    };
  }

  private syncClassMethodsSelectionFromFlags(): void {
    const selected: string[] = [];
    if (this.classMethods.toString) {
      selected.push('toString');
    }
    if (this.classMethods.hashCode) {
      selected.push('hashcode');
    }
    if (this.classMethods.equals) {
      selected.push('equals');
    }
    if (this.classMethods.noArgsConstructor) {
      selected.push('NoArgsConstructor');
    }
    if (this.classMethods.allArgsConstructor) {
      selected.push('AllArgsConstructor');
    }
    if (this.classMethods.builder) {
      selected.push('Builder');
    }
    this.selectedClassMethods = selected;
  }

  private focusFirstErrorField(): void {
    setTimeout(() => {
      if (this.nameError) {
        const nameInput = document.querySelector('.add-entity-container input[placeholder=\"Data object name\"]') as HTMLElement | null;
        nameInput?.focus();
        return;
      }
      if (this.propertiesError) {
        const addPropertyButton = document.querySelector('.add-field-btn') as HTMLElement | null;
        addPropertyButton?.focus();
      }
    });
  }
}
