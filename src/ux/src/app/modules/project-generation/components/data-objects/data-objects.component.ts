import { Component, ElementRef, EventEmitter, Input, OnChanges, OnInit, Output, QueryList, SimpleChanges, ViewChild, ViewChildren } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ModalComponent } from '../../../../components/modal/modal.component';
import { ConfirmationModalComponent } from '../../../../components/confirmation-modal/confirmation-modal.component';
import { EntityDetailViewComponent } from '../entity-detail-view/entity-detail-view.component';
import { PreviewEntitiesComponent } from '../../../../components/preview-entities/preview-entities.component';
import {
  SearchSortComponent,
  SearchConfig,
  SearchSortEvent,
  SortOption
} from '../../../../components/search-sort/search-sort.component';
import { Field } from '../field-item/field-item.component';
import { AddDataObjectComponent } from '../add-data-object/add-data-object.component';
import { InfoBannerComponent } from '../../../../components/info-banner/info-banner.component';
import {
  findReservedJavaOrDatabaseKeyword,
  isValidJavaEnumConstantName,
  isValidJavaTypeName
} from '../../validators/naming-validation';
import { AddMapperComponent, MapperDefinition } from '../add-mapper/add-mapper.component';

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

interface EnumDefinition {
  name: string;
  storage?: 'STRING' | 'ORDINAL';
  constants: string[];
}

interface EntityModel {
  name: string;
  fields?: Array<{ name?: string; type?: string }>;
}

interface MapperCardItem {
  name: string;
  dtoType?: 'request' | 'response';
  metaTag?: string;
  fields: Field[];
}

@Component({
  selector: 'app-data-objects',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatIconModule,
    MatTooltipModule,
    ModalComponent,
    ConfirmationModalComponent,
    EntityDetailViewComponent,
    PreviewEntitiesComponent,
    SearchSortComponent,
    AddDataObjectComponent,
    AddMapperComponent,
    InfoBannerComponent
  ],
  templateUrl: './data-objects.component.html',
  styleUrls: ['./data-objects.component.css']
})
export class DataObjectsComponent implements OnInit, OnChanges {
  @Input() dataObjects: DataObject[] = [];
  @Input() entities: EntityModel[] = [];
  @Input() enums: EnumDefinition[] = [];
  @Input() mappers: MapperDefinition[] = [];
  @Input() defaultTab: 'dataObjects' | 'enums' | 'mappers' = 'dataObjects';

  @Output() dataObjectsChange = new EventEmitter<DataObject[]>();
  @Output() entitiesChange = new EventEmitter<EntityModel[]>();
  @Output() enumsChange = new EventEmitter<EnumDefinition[]>();
  @Output() mappersChange = new EventEmitter<MapperDefinition[]>();
  @Output() activeTabChange = new EventEmitter<'dataObjects' | 'enums' | 'mappers'>();

  @ViewChild(AddDataObjectComponent) addDataObjectComponent!: AddDataObjectComponent;
  @ViewChild(AddMapperComponent) addMapperComponent!: AddMapperComponent;
  @ViewChildren('enumConstantInput') enumConstantInputs!: QueryList<ElementRef<HTMLInputElement>>;

  get enumTypeNames(): string[] {
    return (this.enums ?? []).map(item => String(item?.name ?? '').trim()).filter(Boolean);
  }

  dataObjectsExpanded = true;
  enumsExpanded = true;
  showAddDataObjectModal = false;
  showDeleteConfirmation = false;
  showDataObjectFilters = false;

  showAddEnumModal = false;
  showEnumDeleteConfirmation = false;
  showEnumFilters = false;
  showEnumConstantsModal = false;
  showAddMapperModal = false;
  showMapperDeleteConfirmation = false;

  editingDataObject: DataObject | null = null;
  editingDataObjectIndex: number | null = null;

  deletingDataObjectIndex: number | null = null;
  deletingEnumIndex: number | null = null;

  showDataObjectDetailModal = false;
  viewingDataObject: DataObject | null = null;
  viewingDataObjectIndex: number | null = null;
  viewingDetailCanDeleteFields = true;
  viewingDetailType: 'dataObject' | 'mapper' = 'dataObject';
  showPropertyDeleteConfirmation = false;
  propertyDeleteIndex: number | null = null;

  editingEnum: EnumDefinition | null = null;
  editingEnumIndex: number | null = null;
  enumDraftName = '';
  enumDraftStorage: 'STRING' | 'ORDINAL' = 'STRING';
  enumDraftConstants: string[] = [''];
  enumNameError = '';
  enumConstantsError = '';
  viewingEnumName = '';
  viewingEnumIndex: number | null = null;
  viewingEnumConstants: string[] = [];
  editingMapper: MapperDefinition | null = null;
  editingMapperIndex: number | null = null;
  deletingMapperIndex: number | null = null;
  visibleMapperCards: MapperCardItem[] = [];
  activeTab: 'dataObjects' | 'enums' | 'mappers' = 'dataObjects';

  dataObjectSearchTerm = '';
  dataObjectSortOption: SortOption | null = null;
  visibleDataObjects: DataObject[] = [];

  enumSearchTerm = '';
  enumSortOption: SortOption | null = null;
  visibleEnums: EnumDefinition[] = [];

  dataObjectSearchConfig: SearchConfig = {
    placeholder: 'Search data objects by name...',
    properties: ['name']
  };

  dataObjectSortOptions: SortOption[] = [
    { label: 'Name (A-Z)', property: 'name', direction: 'asc' },
    { label: 'Name (Z-A)', property: 'name', direction: 'desc' }
  ];

  enumSearchConfig: SearchConfig = {
    placeholder: 'Search enums by name...',
    properties: ['name']
  };

  enumSortOptions: SortOption[] = [
    { label: 'Name (A-Z)', property: 'name', direction: 'asc' },
    { label: 'Name (Z-A)', property: 'name', direction: 'desc' },
    { label: 'Mapped (High-Low)', property: 'mappedCount', direction: 'desc' },
    { label: 'Mapped (Low-High)', property: 'mappedCount', direction: 'asc' }
  ];

  deleteModalConfig = {
    title: 'Delete Data Object',
    message: [''],
    buttons: [
      { text: 'Cancel', type: 'cancel' as const, action: 'cancel' as const },
      { text: 'Delete', type: 'danger' as const, action: 'confirm' as const }
    ]
  };

  propertyDeleteModalConfig = {
    title: 'Delete Property',
    message: [''],
    buttons: [
      { text: 'Cancel', type: 'cancel' as const, action: 'cancel' as const },
      { text: 'Delete', type: 'danger' as const, action: 'confirm' as const }
    ]
  };

  enumDeleteModalConfig = {
    title: 'Delete Enum',
    message: 'All Columns Mapped with the ENUM will be deleted in the entities. Proceed further ?',
    buttons: [
      { text: 'Confirm', type: 'danger' as const, action: 'confirm' as const },
      { text: 'Cancel', type: 'cancel' as const, action: 'cancel' as const }
    ]
  };
  mapperDeleteModalConfig = {
    title: 'Delete Mapper',
    message: ['Are you sure you want to delete this mapper?'],
    buttons: [
      { text: 'Cancel', type: 'cancel' as const, action: 'cancel' as const },
      { text: 'Delete', type: 'danger' as const, action: 'confirm' as const }
    ]
  };

  ngOnInit(): void {
    this.applyDefaultTab(this.defaultTab);
    this.activeTabChange.emit(this.activeTab);
    this.updateVisibleDataObjects();
    this.updateVisibleEnums();
    this.updateVisibleMappers();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['dataObjects']) {
      this.updateVisibleDataObjects();
    }
    if (changes['enums'] || changes['entities']) {
      this.updateVisibleEnums();
    }
    if (changes['mappers'] || changes['dataObjects'] || changes['entities']) {
      this.updateVisibleMappers();
    }
    if (changes['defaultTab'] && changes['defaultTab'].currentValue) {
      this.applyDefaultTab(changes['defaultTab'].currentValue);
      this.activeTabChange.emit(this.activeTab);
    }
  }

  toggleDataObjectsPanel(): void {
    this.dataObjectsExpanded = !this.dataObjectsExpanded;
  }

  toggleEnumsPanel(): void {
    this.enumsExpanded = !this.enumsExpanded;
  }

  setActiveTab(tab: 'dataObjects' | 'enums' | 'mappers'): void {
    this.activeTab = tab;
    this.activeTabChange.emit(this.activeTab);
  }

  private applyDefaultTab(tab: 'dataObjects' | 'enums' | 'mappers'): void {
    if (tab === 'mappers') {
      this.activeTab = 'mappers';
      return;
    }

    this.activeTab = tab;
    if (tab === 'enums') {
      this.enumsExpanded = true;
      this.dataObjectsExpanded = false;
    } else {
      this.dataObjectsExpanded = true;
      this.enumsExpanded = true;
    }
  }

  addDataObject(): void {
    this.editingDataObject = null;
    this.editingDataObjectIndex = null;
    this.showAddDataObjectModal = true;
  }

  editDataObject(dataObject: DataObject, index: number): void {
    this.editingDataObject = JSON.parse(JSON.stringify(dataObject));
    this.editingDataObjectIndex = index;
    this.showAddDataObjectModal = true;
  }

  deleteDataObject(index: number): void {
    this.deletingDataObjectIndex = index;
    this.deleteModalConfig.message = [
      `Are you sure you want to delete the data object "${this.dataObjects[index].name}"?`,
      'This action cannot be undone and all associated properties will be removed.'
    ];
    this.showDeleteConfirmation = true;
  }

  addMapper(): void {
    this.editingMapper = null;
    this.editingMapperIndex = null;
    this.showAddMapperModal = true;
  }

  editMapper(mapper: MapperDefinition, index: number): void {
    this.editingMapper = JSON.parse(JSON.stringify(mapper));
    this.editingMapperIndex = index;
    this.showAddMapperModal = true;
  }

  deleteMapper(index: number): void {
    this.deletingMapperIndex = index;
    const mapperName = this.mappers[index]?.name ?? 'this mapper';
    this.mapperDeleteModalConfig.message = [`Are you sure you want to delete mapper "${mapperName}"?`];
    this.showMapperDeleteConfirmation = true;
  }

  confirmDeleteMapper(): void {
    if (this.deletingMapperIndex === null) {
      this.cancelDeleteMapper();
      return;
    }
    this.mappers.splice(this.deletingMapperIndex, 1);
    this.emitMappers();
    this.updateVisibleMappers();
    this.cancelDeleteMapper();
  }

  cancelDeleteMapper(): void {
    this.showMapperDeleteConfirmation = false;
    this.deletingMapperIndex = null;
  }

  onMapperSave(mapper: MapperDefinition): void {
    if (this.editingMapperIndex !== null) {
      this.mappers[this.editingMapperIndex] = mapper;
    } else {
      this.mappers.push(mapper);
    }
    this.activeTab = 'mappers';
    this.emitMappers();
    this.updateVisibleMappers();
    this.activeTabChange.emit(this.activeTab);
    this.onMapperCancel();
  }

  onMapperCancel(): void {
    this.showAddMapperModal = false;
    this.editingMapper = null;
    this.editingMapperIndex = null;
  }

  saveMapper(): void {
    if (this.addMapperComponent) {
      this.addMapperComponent.onSave();
    }
  }

  editMapperFromCard(event: { entity: MapperCardItem; index: number }): void {
    const index = event.index;
    if (index >= 0 && index < this.mappers.length) {
      this.editMapper(this.mappers[index], index);
    }
  }

  deleteMapperFromCard(event: { entity: MapperCardItem; index: number }): void {
    const index = event.index;
    if (index >= 0 && index < this.mappers.length) {
      this.deleteMapper(index);
    }
  }

  confirmDelete(): void {
    if (this.deletingDataObjectIndex !== null) {
      this.dataObjects.splice(this.deletingDataObjectIndex, 1);
      if (this.dataObjects.length === 0) {
        this.showDataObjectFilters = false;
      }
      this.emitDataObjects();
      this.updateVisibleDataObjects();
    }
    this.cancelDelete();
  }

  cancelDelete(): void {
    this.showDeleteConfirmation = false;
    this.deletingDataObjectIndex = null;
  }

  onDataObjectSave(dataObject: DataObject): void {
    if (this.editingDataObjectIndex !== null) {
      this.dataObjects[this.editingDataObjectIndex] = dataObject;
    } else {
      this.dataObjects.push(dataObject);
    }

    this.emitDataObjects();
    this.updateVisibleDataObjects();
    this.showAddDataObjectModal = false;
    this.editingDataObject = null;
    this.editingDataObjectIndex = null;
  }

  onDataObjectCancel(): void {
    this.showAddDataObjectModal = false;
    this.editingDataObject = null;
    this.editingDataObjectIndex = null;
  }

  closeModal(): void {
    this.showAddDataObjectModal = false;
    this.editingDataObject = null;
    this.editingDataObjectIndex = null;
  }

  saveDataObject(): void {
    if (this.addDataObjectComponent) {
      this.addDataObjectComponent.onSave();
    }
  }

  toggleDataObjectFilters(): void {
    if (this.dataObjects.length === 0) {
      return;
    }
    this.showDataObjectFilters = !this.showDataObjectFilters;
    if (!this.showDataObjectFilters) {
      this.dataObjectSearchTerm = '';
      this.dataObjectSortOption = null;
      this.updateVisibleDataObjects();
    }
  }

  onDataObjectSearchSortChange(event: SearchSortEvent): void {
    this.dataObjectSearchTerm = event.searchTerm;
    this.dataObjectSortOption = event.sortOption;
    this.updateVisibleDataObjects();
  }

  openDataObjectDetail(event: { entity: DataObject; index: number }): void {
    const index = this.getDataObjectIndex(event.entity);
    if (index !== null) {
      this.viewingDataObjectIndex = index;
      this.viewingDataObject = this.dataObjects[index];
      this.viewingDetailCanDeleteFields = true;
      this.viewingDetailType = 'dataObject';
      this.showDataObjectDetailModal = true;
    }
  }

  openMapperDetail(event: { entity: MapperCardItem; index: number }): void {
    const index = event.index;
    if (index < 0 || index >= this.mappers.length) {
      return;
    }
    const mapper = this.mappers[index];
    const mappings = Array.isArray(mapper?.mappings) ? mapper.mappings : [];
    this.viewingDataObjectIndex = null;
    this.viewingDataObject = {
      name: String(mapper?.name ?? '').trim(),
      fields: mappings.map((item: any) => ({
        type: String(item?.sourceField ?? '').trim() || '-',
        name: String(item?.targetField ?? '').trim() || '-'
      }))
    };
    this.viewingDetailCanDeleteFields = false;
    this.viewingDetailType = 'mapper';
    this.showDataObjectDetailModal = true;
  }

  closeDataObjectDetail(): void {
    this.showDataObjectDetailModal = false;
    this.viewingDataObject = null;
    this.viewingDataObjectIndex = null;
    this.viewingDetailCanDeleteFields = true;
    this.viewingDetailType = 'dataObject';
  }

  deleteProperty(fieldIndex: number): void {
    if (this.viewingDataObjectIndex === null) {
      return;
    }

    const target = this.dataObjects[this.viewingDataObjectIndex];
    const propertyName = target?.fields?.[fieldIndex]?.name ?? 'this property';
    this.propertyDeleteIndex = fieldIndex;
    this.propertyDeleteModalConfig.message = [
      `Are you sure you want to delete "${propertyName}"?`,
      'This action cannot be undone.'
    ];
    this.showPropertyDeleteConfirmation = true;
  }

  confirmDeleteProperty(): void {
    if (this.viewingDataObjectIndex === null || this.propertyDeleteIndex === null) {
      this.cancelDeleteProperty();
      return;
    }

    const target = this.dataObjects[this.viewingDataObjectIndex];
    if (target?.fields) {
      target.fields.splice(this.propertyDeleteIndex, 1);
      this.emitDataObjects();
      this.updateVisibleDataObjects();
    }

    this.cancelDeleteProperty();
  }

  cancelDeleteProperty(): void {
    this.showPropertyDeleteConfirmation = false;
    this.propertyDeleteIndex = null;
  }

  editDataObjectFromList(event: { entity: DataObject; index: number }): void {
    const index = this.getDataObjectIndex(event.entity);
    if (index !== null) {
      this.editDataObject(this.dataObjects[index], index);
    }
  }

  deleteDataObjectFromList(event: { entity: DataObject; index: number }): void {
    const index = this.getDataObjectIndex(event.entity);
    if (index !== null) {
      this.deleteDataObject(index);
    }
  }

  toggleEnumFilters(): void {
    if (this.enums.length === 0) {
      return;
    }
    this.showEnumFilters = !this.showEnumFilters;
    if (!this.showEnumFilters) {
      this.enumSearchTerm = '';
      this.enumSortOption = null;
      this.updateVisibleEnums();
    }
  }

  onEnumSearchSortChange(event: SearchSortEvent): void {
    this.enumSearchTerm = event.searchTerm;
    this.enumSortOption = event.sortOption;
    this.updateVisibleEnums();
  }

  addEnum(): void {
    this.editingEnum = null;
    this.editingEnumIndex = null;
    this.enumDraftName = '';
    this.enumDraftStorage = 'STRING';
    this.enumDraftConstants = [''];
    this.enumNameError = '';
    this.enumConstantsError = '';
    this.showAddEnumModal = true;
  }

  editEnum(enumItem: EnumDefinition, index: number): void {
    this.editingEnum = JSON.parse(JSON.stringify(enumItem));
    this.editingEnumIndex = index;
    this.enumDraftName = enumItem.name;
    this.enumDraftStorage = enumItem.storage === 'ORDINAL' ? 'ORDINAL' : 'STRING';
    this.enumDraftConstants = enumItem.constants?.length ? [...enumItem.constants] : [''];
    this.enumNameError = '';
    this.enumConstantsError = '';
    this.showAddEnumModal = true;
  }

  closeEnumModal(): void {
    this.showAddEnumModal = false;
    this.editingEnum = null;
    this.editingEnumIndex = null;
    this.enumDraftName = '';
    this.enumDraftStorage = 'STRING';
    this.enumDraftConstants = [''];
    this.enumNameError = '';
    this.enumConstantsError = '';
  }

  addEnumConstant(): void {
    this.enumDraftConstants.push('');
  }

  removeEnumConstant(index: number): void {
    if (this.enumDraftConstants.length <= 1) {
      this.enumDraftConstants[0] = '';
      return;
    }
    this.enumDraftConstants.splice(index, 1);
  }

  moveEnumConstantUp(index: number): void {
    if (index <= 0 || index >= this.enumDraftConstants.length) {
      return;
    }
    [this.enumDraftConstants[index - 1], this.enumDraftConstants[index]] =
      [this.enumDraftConstants[index], this.enumDraftConstants[index - 1]];
  }

  moveEnumConstantDown(index: number): void {
    if (index < 0 || index >= this.enumDraftConstants.length - 1) {
      return;
    }
    [this.enumDraftConstants[index], this.enumDraftConstants[index + 1]] =
      [this.enumDraftConstants[index + 1], this.enumDraftConstants[index]];
  }

  onEnumConstantEnter(index: number, event: Event): void {
    event.preventDefault();
    const current = this.enumDraftConstants[index] ?? '';
    const trimmed = current.trim();
    if (!trimmed) {
      return;
    }
    this.enumDraftConstants[index] = trimmed;
    const nextIndex = index + 1;
    this.enumDraftConstants.splice(nextIndex, 0, '');
    this.enumConstantsError = '';
    setTimeout(() => {
      const input = this.enumConstantInputs?.get(nextIndex)?.nativeElement;
      input?.focus();
    }, 0);
  }

  saveEnum(): void {
    const cleanedConstants = this.enumDraftConstants.map(item => item.trim()).filter(Boolean);
    this.enumDraftConstants = cleanedConstants.length ? cleanedConstants : [''];

    if (!this.validateEnumDraft()) {
      return;
    }

    const normalized: EnumDefinition = {
      name: this.enumDraftName.trim(),
      storage: this.enumDraftStorage,
      constants: this.enumDraftConstants.map(item => item.trim()).filter(Boolean)
    };

    if (this.editingEnumIndex !== null) {
      this.enums[this.editingEnumIndex] = normalized;
    } else {
      this.enums.push(normalized);
    }

    this.emitEnums();
    this.updateVisibleEnums();
    this.closeEnumModal();
  }

  deleteEnum(index: number): void {
    this.deletingEnumIndex = index;
    this.showEnumDeleteConfirmation = true;
  }

  cancelEnumDelete(): void {
    this.showEnumDeleteConfirmation = false;
    this.deletingEnumIndex = null;
  }

  confirmEnumDelete(): void {
    if (this.deletingEnumIndex === null) {
      this.cancelEnumDelete();
      return;
    }

    const enumItem = this.enums[this.deletingEnumIndex];
    const enumName = enumItem?.name;

    if (enumName) {
      this.entities = this.entities.map(entity => ({
        ...entity,
        fields: (entity.fields ?? []).filter(field => String(field?.type ?? '').trim() !== enumName)
      }));
      this.emitEntities();
    }

    this.enums.splice(this.deletingEnumIndex, 1);
    this.emitEnums();
    this.updateVisibleEnums();
    this.cancelEnumDelete();
  }

  viewAllEnumConstants(enumItem: EnumDefinition): void {
    this.viewingEnumIndex = this.getEnumIndexByName(enumItem.name);
    this.viewingEnumName = enumItem.name;
    this.viewingEnumConstants = [...(enumItem.constants ?? [])];
    this.showEnumConstantsModal = true;
  }

  deleteViewedEnumConstant(index: number): void {
    if (this.viewingEnumIndex === null) {
      return;
    }
    const target = this.enums[this.viewingEnumIndex];
    if (!target?.constants || target.constants.length <= 1) {
      return;
    }
    target.constants.splice(index, 1);
    this.viewingEnumConstants = [...target.constants];
    this.emitEnums();
    this.updateVisibleEnums();
  }

  moveViewedEnumConstantUp(index: number): void {
    if (this.viewingEnumIndex === null || index <= 0) {
      return;
    }
    const target = this.enums[this.viewingEnumIndex];
    if (!target?.constants || index >= target.constants.length) {
      return;
    }
    [target.constants[index - 1], target.constants[index]] = [target.constants[index], target.constants[index - 1]];
    this.viewingEnumConstants = [...target.constants];
    this.emitEnums();
    this.updateVisibleEnums();
  }

  moveViewedEnumConstantDown(index: number): void {
    if (this.viewingEnumIndex === null) {
      return;
    }
    const target = this.enums[this.viewingEnumIndex];
    if (!target?.constants || index < 0 || index >= target.constants.length - 1) {
      return;
    }
    [target.constants[index], target.constants[index + 1]] = [target.constants[index + 1], target.constants[index]];
    this.viewingEnumConstants = [...target.constants];
    this.emitEnums();
    this.updateVisibleEnums();
  }

  closeEnumConstantsModal(): void {
    this.showEnumConstantsModal = false;
    this.viewingEnumName = '';
    this.viewingEnumIndex = null;
    this.viewingEnumConstants = [];
  }

  shouldShowEnumMappedHelp(): boolean {
    if (!this.editingEnum || !this.editingEnum.name) {
      return false;
    }
    return this.getMappedEntityCount(this.editingEnum.name) > 0;
  }

  getEnumMappedHelpText(): string {
    if (!this.editingEnum || !this.editingEnum.name) {
      return '';
    }
    const count = this.getMappedEntityCount(this.editingEnum.name);
    return `${count} entities mapped with enum as field`;
  }

  getMappedEntityCount(enumName: string): number {
    const normalized = String(enumName ?? '').trim();
    if (!normalized) {
      return 0;
    }
    return this.entities.filter(entity =>
      (entity.fields ?? []).some(field => String(field?.type ?? '').trim() === normalized)
    ).length;
  }

  getEnumPreviewConstants(constants: string[]): string[] {
    return (constants ?? []).slice(0, 5);
  }

  private validateEnumDraft(): boolean {
    this.enumNameError = '';
    this.enumConstantsError = '';

    const enumName = this.enumDraftName.trim();
    if (!enumName) {
      this.enumNameError = 'Enum name is required.';
      return false;
    }

    if (!isValidJavaTypeName(enumName)) {
      this.enumNameError = 'Enum name must follow Java type naming (PascalCase, valid identifier).';
      return false;
    }

    const reserved = findReservedJavaOrDatabaseKeyword(enumName);
    if (reserved) {
      this.enumNameError = `Your input contains the keyword "${reserved}", which cannot be used in java/database context.`;
      return false;
    }

    const normalizedName = enumName.toLowerCase();
    const duplicateEnum = this.enums.find((item, index) => {
      if (this.editingEnumIndex !== null && index === this.editingEnumIndex) {
        return false;
      }
      return String(item?.name ?? '').trim().toLowerCase() === normalizedName;
    });

    if (duplicateEnum) {
      this.enumNameError = `Enum "${enumName}" already exists.`;
      return false;
    }

    const constants = this.enumDraftConstants.map(item => item.trim()).filter(Boolean);
    if (constants.length === 0) {
      this.enumConstantsError = 'At least one constant is needed for enum.';
      return false;
    }

    const invalidConstant = constants.find(item => !isValidJavaEnumConstantName(item));
    if (invalidConstant) {
      this.enumConstantsError = `Constant \"${invalidConstant}\" must be in Upper Case.`;
      return false;
    }

    const unique = new Set(constants.map(item => item.toLowerCase()));
    if (unique.size !== constants.length) {
      this.enumConstantsError = 'Duplicate constants are not allowed.';
      return false;
    }

    return true;
  }

  private updateVisibleDataObjects(): void {
    const normalizedSearch = this.dataObjectSearchTerm.trim().toLowerCase();
    let results = [...this.dataObjects];

    if (normalizedSearch) {
      results = results.filter(dataObject =>
        dataObject.name?.toLowerCase().includes(normalizedSearch)
      );
    }

    if (this.dataObjectSortOption) {
      const direction = this.dataObjectSortOption.direction;
      results = results.sort((a, b) => {
        const aValue = a.name?.toLowerCase() ?? '';
        const bValue = b.name?.toLowerCase() ?? '';
        const comparison = aValue < bValue ? -1 : aValue > bValue ? 1 : 0;
        return direction === 'asc' ? comparison : -comparison;
      });
    }

    this.visibleDataObjects = results;
  }

  private updateVisibleEnums(): void {
    const normalizedSearch = this.enumSearchTerm.trim().toLowerCase();
    let results = [...this.enums];

    if (normalizedSearch) {
      results = results.filter(item =>
        item.name?.toLowerCase().includes(normalizedSearch)
      );
    }

    if (this.enumSortOption) {
      const direction = this.enumSortOption.direction;
      const sortBy = this.enumSortOption.property;
      results = results.sort((left, right) => {
        if (sortBy === 'mappedCount') {
          const leftCount = this.getMappedEntityCount(left.name);
          const rightCount = this.getMappedEntityCount(right.name);
          const comparison = leftCount - rightCount;
          return direction === 'asc' ? comparison : -comparison;
        }

        const leftValue = left.name?.toLowerCase() ?? '';
        const rightValue = right.name?.toLowerCase() ?? '';
        const comparison = leftValue < rightValue ? -1 : leftValue > rightValue ? 1 : 0;
        return direction === 'asc' ? comparison : -comparison;
      });
    }

    this.visibleEnums = results;
  }

  private getEnumIndexByName(enumName: string): number | null {
    const normalizedName = String(enumName ?? '').trim().toLowerCase();
    if (!normalizedName) {
      return null;
    }
    const index = this.enums.findIndex(item => String(item?.name ?? '').trim().toLowerCase() === normalizedName);
    return index >= 0 ? index : null;
  }

  private getDataObjectIndex(dataObject: DataObject): number | null {
    const name = dataObject?.name?.toLowerCase();
    if (!name) {
      return null;
    }

    const index = this.dataObjects.findIndex(item => item.name?.toLowerCase() === name);
    return index >= 0 ? index : null;
  }

  private emitDataObjects(): void {
    this.dataObjectsChange.emit(JSON.parse(JSON.stringify(this.dataObjects)));
  }

  private emitEntities(): void {
    this.entitiesChange.emit(JSON.parse(JSON.stringify(this.entities)));
  }

  private emitEnums(): void {
    this.enumsChange.emit(JSON.parse(JSON.stringify(this.enums)));
  }

  private updateVisibleMappers(): void {
    this.visibleMapperCards = (this.mappers ?? []).map((mapper) => {
      const mappings = Array.isArray(mapper?.mappings) ? mapper.mappings : [];
      const fields: Field[] = mappings.map((item) => ({ type: 'String', name: `${item.sourceField} -> ${item.targetField}` }));
      return {
        name: String(mapper?.name ?? '').trim(),
        dtoType: undefined,
        metaTag: `${mappings.length} mappings`,
        fields
      };
    }).filter((item) => item.name);
  }

  private emitMappers(): void {
    this.mappersChange.emit(JSON.parse(JSON.stringify(this.mappers)));
  }

  trackByIndex(index: number): number {
    return index;
  }
}
