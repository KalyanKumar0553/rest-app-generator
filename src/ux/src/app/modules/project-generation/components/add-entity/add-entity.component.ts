import { Component, EventEmitter, Input, Output, OnChanges, SimpleChanges, ViewChild } from '@angular/core';
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
import { HelpPopoverComponent } from '../../../../components/help-popover/help-popover.component';
import { RestConfigComponent, RestEndpointConfig } from '../rest-config/rest-config.component';

interface Entity {
  name: string;
  mappedSuperclass: boolean;
  addRestEndpoints: boolean;
  addCrudOperations?: boolean;
  restConfig?: RestEndpointConfig;
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
    HelpPopoverComponent,
    RestConfigComponent,
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
  @Input() dataObjects: Array<{ name?: string; dtoType?: 'request' | 'response'; fields?: Field[] }> = [];
  @Output() save = new EventEmitter<any>();
  @Output() cancel = new EventEmitter<void>();

  entityName = '';
  mappedSuperclass = false;
  addRestEndpoints = false;
  addCrudOperations = false;
  auditable = false;
  softDelete = false;
  immutable = false;
  naturalIdCache = false;
  restConfig: RestEndpointConfig = this.getDefaultRestConfig();
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
  isConfigureRestOpen = false;
  showDisableRestConfirmation = false;
  @ViewChild('configureRestComponent') configureRestComponent?: RestConfigComponent;

  fieldDeleteModalConfig = {
    title: 'Delete field',
    message: ['Are you sure you want to delete this field?', 'This action cannot be undone.'],
    buttons: [
      { text: 'Cancel', type: 'cancel' as const, action: 'cancel' as const },
      { text: 'Delete', type: 'danger' as const, action: 'confirm' as const }
    ] as ModalButton[]
  };

  disableRestModalConfig = {
    title: 'Confirmation',
    message: ['All configured API changes will be lost. Do you want to proceed?'],
    buttons: [
      { text: 'Continue', type: 'danger' as const, action: 'confirm' as const },
      { text: 'Cancel', type: 'cancel' as const, action: 'cancel' as const }
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
    this.addCrudOperations = Boolean(entity.addCrudOperations);
    this.restConfig = this.parseRestConfig(entity.restConfig);
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
    this.addCrudOperations = false;
    this.restConfig = this.getDefaultRestConfig();
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
      addCrudOperations: this.addCrudOperations,
      restConfig: this.addRestEndpoints ? this.parseRestConfig(this.restConfig) : undefined,
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
      this.addCrudOperations = false;
      this.isConfigureRestOpen = false;
      this.closeFieldConfig();
    }
    this.updateVisibleFields();
  }

  onAddRestEndpointsChange(): void {
    if (this.addRestEndpoints) {
      this.mappedSuperclass = false;
      this.addCrudOperations = false;
    } else {
      this.addRestEndpoints = true;
      this.showDisableRestConfirmation = true;
      return;
    }
    this.updateVisibleFields();
  }

  confirmDisableRestEndpoints(): void {
    this.addRestEndpoints = false;
    this.restConfig = this.getDefaultRestConfig();
    this.isConfigureRestOpen = false;
    this.showDisableRestConfirmation = false;
    this.updateVisibleFields();
  }

  cancelDisableRestEndpoints(): void {
    this.addRestEndpoints = true;
    this.showDisableRestConfirmation = false;
    this.updateVisibleFields();
  }

  onAddCrudOperationsChange(): void {
    if (this.addCrudOperations) {
      this.mappedSuperclass = false;
      this.addRestEndpoints = false;
      this.isConfigureRestOpen = false;
    }
    this.updateVisibleFields();
  }

  openConfigureRestModal(): void {
    if (!this.addRestEndpoints) {
      return;
    }
    this.isConfigureRestOpen = true;
  }

  saveConfigureRest(): void {
    this.configureRestComponent?.saveConfig();
  }

  onConfigureRestSave(config: RestEndpointConfig): void {
    this.restConfig = this.parseRestConfig(config);
    this.isConfigureRestOpen = false;
  }

  onConfigureRestCancel(): void {
    this.isConfigureRestOpen = false;
  }

  isEntityOptionVisible(option: 'mappedSuperclass' | 'addRestEndpoints' | 'addCrudOperations'): boolean {
    const selected = this.mappedSuperclass || this.addRestEndpoints || this.addCrudOperations;
    if (!selected) {
      return true;
    }
    return Boolean((this as Record<string, unknown>)[option]);
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
    if (this.mappedSuperclass) {
      this.addRestEndpoints = false;
      this.addCrudOperations = false;
      return;
    }
    if (this.addRestEndpoints) {
      this.addCrudOperations = false;
    }
  }

  private getDefaultRestConfig(): RestEndpointConfig {
    return {
      resourceName: 'Employee',
      basePath: '/api/employees',
      methods: {
        list: true,
        get: true,
        create: true,
        update: true,
        patch: true,
        delete: true,
        bulkInsert: true,
        bulkUpdate: true,
        bulkDelete: true
      },
      apiVersioning: {
        enabled: true,
        strategy: 'header',
        headerName: 'X-API-VERSION',
        defaultVersion: '1'
      },
      pathVariableType: 'UUID',
      deletion: {
        mode: 'SOFT',
        restoreEndpoint: true,
        includeDeletedParam: true
      },
      hateoas: {
        enabled: true,
        selfLink: true,
        updateLink: true,
        deleteLink: true
      },
      pagination: {
        enabled: true,
        mode: 'OFFSET',
        sortField: 'createdAt',
        sortDirection: 'DESC'
      },
      searchFiltering: {
        keywordSearch: true,
        jpaSpecification: true,
        searchableFields: []
      },
      batchOperations: {
        insert: {
          batchSize: 500,
          enableAsyncMode: false
        },
        update: {
          batchSize: 500,
          updateMode: 'PUT',
          optimisticLockHandling: 'FAIL_ON_CONFLICT',
          validationStrategy: 'VALIDATE_ALL_FIRST',
          enableAsyncMode: false,
          asyncProcessing: true
        },
        bulkDelete: {
          deletionStrategy: 'SOFT',
          batchSize: 1000,
          failureStrategy: 'STOP_ON_FIRST_ERROR',
          enableAsyncMode: false,
          allowIncludeDeletedParam: true
        }
      },
      requestResponse: {
        request: {
          create: {
            mode: 'GENERATE_DTO',
            dtoName: 'Request'
          },
          update: {
            mode: 'GENERATE_DTO',
            dtoName: 'UpdateRequest'
          },
          patch: {
            mode: 'JSON_MERGE_PATCH'
          },
          bulkInsertType: '',
          bulkUpdateType: '',
          bulkDeleteType: 'List<UUID>'
        },
        response: {
          responseType: 'RESPONSE_ENTITY',
          dtoName: '',
          responseWrapper: 'STANDARD_ENVELOPE',
          enableFieldProjection: true,
          includeHateoasLinks: true
        }
      },
      documentation: {
        endpoints: {
          list: { description: '', descriptionTags: [], deprecated: false },
          get: { description: '', descriptionTags: [], deprecated: false },
          create: { description: '', descriptionTags: [], deprecated: false },
          update: { description: '', descriptionTags: [], deprecated: false },
          patch: { description: '', descriptionTags: [], deprecated: false },
          delete: { description: '', descriptionTags: [], deprecated: false },
          bulkInsert: { description: '', descriptionTags: [], deprecated: false },
          bulkUpdate: { description: '', descriptionTags: [], deprecated: false },
          bulkDelete: { description: '', descriptionTags: [], deprecated: false }
        }
      }
    };
  }

  private parseRestConfig(rawConfig: RestEndpointConfig | null | undefined): RestEndpointConfig {
    const fallback = this.getDefaultRestConfig();
    if (!rawConfig || typeof rawConfig !== 'object') {
      return fallback;
    }

    const legacyPaginationEnabled = typeof (rawConfig as any).pagination === 'boolean'
      ? Boolean((rawConfig as any).pagination)
      : fallback.pagination.enabled;

    return {
      resourceName: String((rawConfig as any).resourceName ?? fallback.resourceName).trim() || fallback.resourceName,
      basePath: String(rawConfig.basePath ?? fallback.basePath).trim() || fallback.basePath,
      methods: {
        list: Boolean((rawConfig as any).methods?.list ?? fallback.methods.list),
        get: Boolean((rawConfig as any).methods?.get ?? fallback.methods.get),
        create: Boolean((rawConfig as any).methods?.create ?? fallback.methods.create),
        update: Boolean((rawConfig as any).methods?.update ?? fallback.methods.update),
        patch: Boolean((rawConfig as any).methods?.patch ?? fallback.methods.patch),
        delete: Boolean((rawConfig as any).methods?.delete ?? fallback.methods.delete),
        bulkInsert: Boolean((rawConfig as any).methods?.bulkInsert ?? fallback.methods.bulkInsert),
        bulkUpdate: Boolean((rawConfig as any).methods?.bulkUpdate ?? fallback.methods.bulkUpdate),
        bulkDelete: Boolean((rawConfig as any).methods?.bulkDelete ?? fallback.methods.bulkDelete)
      },
      apiVersioning: {
        enabled: Boolean((rawConfig as any).apiVersioning?.enabled ?? fallback.apiVersioning.enabled),
        strategy: (rawConfig as any).apiVersioning?.strategy === 'path' ? 'path' : 'header',
        headerName: String((rawConfig as any).apiVersioning?.headerName ?? fallback.apiVersioning.headerName).trim(),
        defaultVersion: String((rawConfig as any).apiVersioning?.defaultVersion ?? fallback.apiVersioning.defaultVersion).trim()
      },
      pathVariableType: (rawConfig as any).pathVariableType === 'LONG'
        ? 'LONG'
        : (rawConfig as any).pathVariableType === 'STRING'
          ? 'STRING'
          : 'UUID',
      deletion: {
        mode: (rawConfig as any).deletion?.mode === 'HARD' ? 'HARD' : 'SOFT',
        restoreEndpoint: Boolean((rawConfig as any).deletion?.restoreEndpoint ?? fallback.deletion.restoreEndpoint),
        includeDeletedParam: Boolean((rawConfig as any).deletion?.includeDeletedParam ?? fallback.deletion.includeDeletedParam)
      },
      hateoas: {
        enabled: Boolean((rawConfig as any).hateoas?.enabled ?? fallback.hateoas.enabled),
        selfLink: Boolean((rawConfig as any).hateoas?.selfLink ?? fallback.hateoas.selfLink),
        updateLink: Boolean((rawConfig as any).hateoas?.updateLink ?? fallback.hateoas.updateLink),
        deleteLink: Boolean((rawConfig as any).hateoas?.deleteLink ?? fallback.hateoas.deleteLink)
      },
      pagination: {
        enabled: Boolean((rawConfig as any).pagination?.enabled ?? legacyPaginationEnabled),
        mode: (rawConfig as any).pagination?.mode === 'CURSOR' ? 'CURSOR' : 'OFFSET',
        sortField: String((rawConfig as any).pagination?.sortField ?? fallback.pagination.sortField).trim(),
        sortDirection: (rawConfig as any).pagination?.sortDirection === 'ASC' ? 'ASC' : 'DESC'
      },
      searchFiltering: {
        keywordSearch: Boolean((rawConfig as any).searchFiltering?.keywordSearch ?? fallback.searchFiltering.keywordSearch),
        jpaSpecification: Boolean((rawConfig as any).searchFiltering?.jpaSpecification ?? fallback.searchFiltering.jpaSpecification),
        searchableFields: Array.isArray((rawConfig as any).searchFiltering?.searchableFields)
          ? (rawConfig as any).searchFiltering.searchableFields
            .map((value: unknown) => String(value ?? '').trim())
            .filter(Boolean)
          : [...fallback.searchFiltering.searchableFields]
      },
      batchOperations: {
        insert: {
          batchSize: Number((rawConfig as any).batchOperations?.insert?.batchSize ?? fallback.batchOperations.insert.batchSize),
          enableAsyncMode: Boolean((rawConfig as any).batchOperations?.insert?.enableAsyncMode ?? fallback.batchOperations.insert.enableAsyncMode)
        },
        update: {
          batchSize: Number((rawConfig as any).batchOperations?.update?.batchSize ?? fallback.batchOperations.update.batchSize),
          updateMode: (rawConfig as any).batchOperations?.update?.updateMode === 'PATCH' ? 'PATCH' : 'PUT',
          optimisticLockHandling: (rawConfig as any).batchOperations?.update?.optimisticLockHandling === 'SKIP_CONFLICTS' ? 'SKIP_CONFLICTS' : 'FAIL_ON_CONFLICT',
          validationStrategy: (rawConfig as any).batchOperations?.update?.validationStrategy === 'SKIP_DUPLICATES' ? 'SKIP_DUPLICATES' : 'VALIDATE_ALL_FIRST',
          enableAsyncMode: Boolean((rawConfig as any).batchOperations?.update?.enableAsyncMode ?? fallback.batchOperations.update.enableAsyncMode),
          asyncProcessing: Boolean((rawConfig as any).batchOperations?.update?.asyncProcessing ?? fallback.batchOperations.update.asyncProcessing)
        },
        bulkDelete: {
          deletionStrategy: (rawConfig as any).batchOperations?.bulkDelete?.deletionStrategy === 'HARD' ? 'HARD' : 'SOFT',
          batchSize: Number((rawConfig as any).batchOperations?.bulkDelete?.batchSize ?? fallback.batchOperations.bulkDelete.batchSize),
          failureStrategy: (rawConfig as any).batchOperations?.bulkDelete?.failureStrategy === 'CONTINUE_AND_REPORT_FAILURES'
            ? 'CONTINUE_AND_REPORT_FAILURES'
            : 'STOP_ON_FIRST_ERROR',
          enableAsyncMode: Boolean((rawConfig as any).batchOperations?.bulkDelete?.enableAsyncMode ?? fallback.batchOperations.bulkDelete.enableAsyncMode),
          allowIncludeDeletedParam: Boolean((rawConfig as any).batchOperations?.bulkDelete?.allowIncludeDeletedParam ?? fallback.batchOperations.bulkDelete.allowIncludeDeletedParam)
        }
      },
      requestResponse: {
        request: {
          create: {
            mode: (rawConfig as any).requestResponse?.request?.create?.mode === 'NONE' ? 'NONE' : 'GENERATE_DTO',
            dtoName: String((rawConfig as any).requestResponse?.request?.create?.dtoName ?? fallback.requestResponse.request.create.dtoName).trim()
          },
          update: {
            mode: (rawConfig as any).requestResponse?.request?.update?.mode === 'NONE' ? 'NONE' : 'GENERATE_DTO',
            dtoName: String((rawConfig as any).requestResponse?.request?.update?.dtoName ?? fallback.requestResponse.request.update.dtoName).trim()
          },
          patch: {
            mode: (rawConfig as any).requestResponse?.request?.patch?.mode === 'JSON_PATCH' ? 'JSON_PATCH' : 'JSON_MERGE_PATCH'
          },
          bulkInsertType: String((rawConfig as any).requestResponse?.request?.bulkInsertType ?? fallback.requestResponse.request.bulkInsertType).trim(),
          bulkUpdateType: String((rawConfig as any).requestResponse?.request?.bulkUpdateType ?? fallback.requestResponse.request.bulkUpdateType).trim(),
          bulkDeleteType: String((rawConfig as any).requestResponse?.request?.bulkDeleteType ?? fallback.requestResponse.request.bulkDeleteType).trim()
        },
        response: {
          responseType: (rawConfig as any).requestResponse?.response?.responseType === 'DTO_DIRECT'
            ? 'DTO_DIRECT'
            : (rawConfig as any).requestResponse?.response?.responseType === 'CUSTOM_WRAPPER'
              ? 'CUSTOM_WRAPPER'
              : 'RESPONSE_ENTITY',
          dtoName: String((rawConfig as any).requestResponse?.response?.dtoName ?? '').trim(),
          responseWrapper: (rawConfig as any).requestResponse?.response?.responseWrapper === 'NONE'
            ? 'NONE'
            : (rawConfig as any).requestResponse?.response?.responseWrapper === 'UPSERT'
              ? 'UPSERT'
              : 'STANDARD_ENVELOPE',
          enableFieldProjection: Boolean((rawConfig as any).requestResponse?.response?.enableFieldProjection ?? fallback.requestResponse.response.enableFieldProjection),
          includeHateoasLinks: Boolean((rawConfig as any).requestResponse?.response?.includeHateoasLinks ?? fallback.requestResponse.response.includeHateoasLinks)
        }
      },
      documentation: {
        endpoints: {
          list: {
            description: String((rawConfig as any).documentation?.endpoints?.list?.description ?? '').trim(),
            descriptionTags: Array.isArray((rawConfig as any).documentation?.endpoints?.list?.descriptionTags)
              ? (rawConfig as any).documentation.endpoints.list.descriptionTags.map((value: unknown) => String(value ?? '').trim()).filter(Boolean)
              : [],
            deprecated: Boolean((rawConfig as any).documentation?.endpoints?.list?.deprecated)
          },
          get: {
            description: String((rawConfig as any).documentation?.endpoints?.get?.description ?? '').trim(),
            descriptionTags: Array.isArray((rawConfig as any).documentation?.endpoints?.get?.descriptionTags)
              ? (rawConfig as any).documentation.endpoints.get.descriptionTags.map((value: unknown) => String(value ?? '').trim()).filter(Boolean)
              : [],
            deprecated: Boolean((rawConfig as any).documentation?.endpoints?.get?.deprecated)
          },
          create: {
            description: String((rawConfig as any).documentation?.endpoints?.create?.description ?? '').trim(),
            descriptionTags: Array.isArray((rawConfig as any).documentation?.endpoints?.create?.descriptionTags)
              ? (rawConfig as any).documentation.endpoints.create.descriptionTags.map((value: unknown) => String(value ?? '').trim()).filter(Boolean)
              : [],
            deprecated: Boolean((rawConfig as any).documentation?.endpoints?.create?.deprecated)
          },
          update: {
            description: String((rawConfig as any).documentation?.endpoints?.update?.description ?? '').trim(),
            descriptionTags: Array.isArray((rawConfig as any).documentation?.endpoints?.update?.descriptionTags)
              ? (rawConfig as any).documentation.endpoints.update.descriptionTags.map((value: unknown) => String(value ?? '').trim()).filter(Boolean)
              : [],
            deprecated: Boolean((rawConfig as any).documentation?.endpoints?.update?.deprecated)
          },
          patch: {
            description: String((rawConfig as any).documentation?.endpoints?.patch?.description ?? '').trim(),
            descriptionTags: Array.isArray((rawConfig as any).documentation?.endpoints?.patch?.descriptionTags)
              ? (rawConfig as any).documentation.endpoints.patch.descriptionTags.map((value: unknown) => String(value ?? '').trim()).filter(Boolean)
              : [],
            deprecated: Boolean((rawConfig as any).documentation?.endpoints?.patch?.deprecated)
          },
          delete: {
            description: String((rawConfig as any).documentation?.endpoints?.delete?.description ?? '').trim(),
            descriptionTags: Array.isArray((rawConfig as any).documentation?.endpoints?.delete?.descriptionTags)
              ? (rawConfig as any).documentation.endpoints.delete.descriptionTags.map((value: unknown) => String(value ?? '').trim()).filter(Boolean)
              : [],
            deprecated: Boolean((rawConfig as any).documentation?.endpoints?.delete?.deprecated)
          },
          bulkInsert: {
            description: String((rawConfig as any).documentation?.endpoints?.bulkInsert?.description ?? '').trim(),
            descriptionTags: Array.isArray((rawConfig as any).documentation?.endpoints?.bulkInsert?.descriptionTags)
              ? (rawConfig as any).documentation.endpoints.bulkInsert.descriptionTags.map((value: unknown) => String(value ?? '').trim()).filter(Boolean)
              : [],
            deprecated: Boolean((rawConfig as any).documentation?.endpoints?.bulkInsert?.deprecated)
          },
          bulkUpdate: {
            description: String((rawConfig as any).documentation?.endpoints?.bulkUpdate?.description ?? '').trim(),
            descriptionTags: Array.isArray((rawConfig as any).documentation?.endpoints?.bulkUpdate?.descriptionTags)
              ? (rawConfig as any).documentation.endpoints.bulkUpdate.descriptionTags.map((value: unknown) => String(value ?? '').trim()).filter(Boolean)
              : [],
            deprecated: Boolean((rawConfig as any).documentation?.endpoints?.bulkUpdate?.deprecated)
          },
          bulkDelete: {
            description: String((rawConfig as any).documentation?.endpoints?.bulkDelete?.description ?? '').trim(),
            descriptionTags: Array.isArray((rawConfig as any).documentation?.endpoints?.bulkDelete?.descriptionTags)
              ? (rawConfig as any).documentation.endpoints.bulkDelete.descriptionTags.map((value: unknown) => String(value ?? '').trim()).filter(Boolean)
              : [],
            deprecated: Boolean((rawConfig as any).documentation?.endpoints?.bulkDelete?.deprecated)
          }
        }
      }
    };
  }
}
