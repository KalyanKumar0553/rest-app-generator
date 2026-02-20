import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Field } from '../field-item/field-item.component';
import { SearchSortComponent, SearchConfig, SortOption, SearchSortEvent } from '../../../../components/search-sort/search-sort.component';
import { ConfirmationModalComponent, ModalButton } from '../../../../components/confirmation-modal/confirmation-modal.component';
import { EditPropertyComponent } from '../edit-property/edit-property.component';
import { ModalComponent } from '../../../../components/modal/modal.component';
import { ValidatorService } from '../../../../services/validator.service';
import { buildEntityNameRules, buildFieldListRules, buildFieldRules } from '../../validators/entity-validation';
import { FieldFilterService } from '../../../../services/field-filter.service';
import { SearchableMultiSelectComponent } from '../../../../components/searchable-multi-select/searchable-multi-select.component';
import { ToastService } from '../../../../services/toast.service';

interface DataObject {
  name: string;
  dtoType?: 'request' | 'response';
  mapperEnabled?: boolean;
  mapperModels?: string[];
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
    MatButtonModule,
    MatIconModule,
    SearchSortComponent,
    SearchableMultiSelectComponent,
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
  mapperEnabled = false;
  mapperModels: string[] = [];
  nameError = '';

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

  private readonly baseFieldTypes = [
    'String',
    'Int',
    'Long',
    'Double',
    'Decimal',
    'Boolean',
    'Date',
    'Time',
    'DateTime',
    'Instant',
    'UUID',
    'Json',
    'Binary',
    'List<String>',
    'List<Long>',
    'List<Integer>'
  ];

  get fieldTypes(): string[] {
    const enums = Array.isArray(this.enumTypes)
      ? this.enumTypes.map(item => String(item ?? '').trim()).filter(Boolean)
      : [];
    return Array.from(new Set([...this.baseFieldTypes, ...enums]));
  }

  constructor(
    private validatorService: ValidatorService,
    private fieldFilterService: FieldFilterService,
    private toastService: ToastService
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

    if (changes['availableModels'] && !this.hasAvailableModels) {
      this.mapperEnabled = false;
      this.mapperModels = [];
    }
  }

  loadDataObject(dataObject: DataObject): void {
    this.dataObjectName = dataObject.name;
    this.dtoType = dataObject.dtoType ?? 'request';
    this.mapperEnabled = Boolean(dataObject.mapperEnabled);
    this.mapperModels = Array.isArray(dataObject.mapperModels) ? [...dataObject.mapperModels] : [];
    this.fields = this.sanitizeProperties(JSON.parse(JSON.stringify(dataObject.fields)));
    this.nameError = '';
    this.updateVisibleFields();
  }

  resetForm(): void {
    this.dataObjectName = '';
    this.dtoType = 'request';
    this.mapperEnabled = false;
    this.mapperModels = [];
    this.nameError = '';
    this.fields = [];
    this.updateVisibleFields();
  }

  addField(): void {
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
      })
    );

    if (valid) {
      this.nameError = '';
    }
    return valid;
  }

  onSave(): void {
    if (!this.validateDataObjectName()) {
      return;
    }

    if (!this.fields?.length) {
      this.toastService.error('At least one property is required to save a data object.');
      return;
    }

    if (!this.validatorService.validate(this, buildFieldListRules({
      requirePrimaryKey: false,
      itemLabel: 'property'
    }))) {
      return;
    }

    this.save.emit({
      name: this.dataObjectName,
      dtoType: this.dtoType,
      mapperEnabled: this.mapperEnabled,
      mapperModels: this.mapperEnabled ? [...this.mapperModels] : [],
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

  onMapperEnabledChange(): void {
    if (!this.mapperEnabled) {
      this.mapperModels = [];
    }
  }

  get mapperModelOptions(): string[] {
    return this.availableModels
      .map(model => model?.name?.trim() ?? '')
      .filter(Boolean)
      .sort((a, b) => a.localeCompare(b));
  }

  get hasAvailableModels(): boolean {
    return this.availableModels
      .map(model => model?.name?.trim() ?? '')
      .filter(Boolean)
      .length > 0;
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
}
