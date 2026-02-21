import { Component, Input, Output, EventEmitter, ViewChild, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from '../../../../components/modal/modal.component';
import { AddEntityComponent } from '../add-entity/add-entity.component';
import { AddRelationComponent, Relation } from '../add-relation/add-relation.component';
import { ConfirmationModalComponent } from '../../../../components/confirmation-modal/confirmation-modal.component';
import { EntityDetailViewComponent } from '../entity-detail-view/entity-detail-view.component';
import { ImportSchemaComponent } from '../import-schema/import-schema.component';
import { ImportSchemaPreviewComponent } from '../import-schema-preview/import-schema-preview.component';
import { PreviewEntitiesComponent } from '../../../../components/preview-entities/preview-entities.component';
import { PreviewRelationsComponent } from '../../../../components/preview-relations/preview-relations.component';
import { SearchSortComponent, SearchConfig, SortOption, SearchSortEvent } from '../../../../components/search-sort/search-sort.component';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { Field } from '../field-item/field-item.component';
import { DdlEntity, DdlImportService } from '../../../../services/ddl-import.service';
import { ToastService } from '../../../../services/toast.service';
import { InfoBannerComponent } from '../../../../components/info-banner/info-banner.component';
import { Router } from '@angular/router';
import { RestEndpointConfig } from '../rest-config/rest-config.component';

interface Entity {
  name: string;
  mappedSuperclass?: boolean;
  addRestEndpoints?: boolean;
  addCrudOperations?: boolean;
  restConfig?: RestEndpointConfig;
  auditable?: boolean;
  softDelete?: boolean;
  immutable?: boolean;
  naturalIdCache?: boolean;
  fields?: Field[];
}


@Component({
  selector: 'app-entities',
  standalone: true,
  imports: [CommonModule, ModalComponent, AddEntityComponent, AddRelationComponent, ConfirmationModalComponent, EntityDetailViewComponent, ImportSchemaComponent, ImportSchemaPreviewComponent, PreviewEntitiesComponent, PreviewRelationsComponent, SearchSortComponent, MatIconModule, MatButtonModule, InfoBannerComponent],
  templateUrl: './entities.component.html',
  styleUrls: ['./entities.component.css']
})
export class EntitiesComponent implements OnInit {
  @Input() entities: Entity[] = [];
  @Input() relations: Relation[] = [];
  @Input() enums: Array<{ name: string }> = [];
  @Input() dataObjects: Array<{ name?: string; dtoType?: 'request' | 'response'; fields?: Field[] }> = [];
  @Output() entitiesChange = new EventEmitter<Entity[]>();
  @ViewChild(AddEntityComponent) addEntityComponent!: AddEntityComponent;
  @ViewChild(AddRelationComponent) addRelationComponent!: AddRelationComponent;
  @ViewChild(SearchSortComponent) entitySearchSortComponent?: SearchSortComponent;

  entitiesExpanded = true;
  relationsExpanded = true;
  showAddEntityModal = false;
  showImportSchemaModal = false;
  showImportSchemaPreviewModal = false;
  importSchemaSql = '';
  importPreviewEntities: DdlEntity[] = [];
  importAddRestEndpoints = false;
  editingEntity: Entity | null = null;
  editingEntityIndex: number | null = null;
  showDeleteConfirmation = false;
  deletingEntityIndex: number | null = null;
  deletingEntityName: string = '';
  showEntityDetailModal = false;
  viewingEntity: Entity | null = null;

  searchConfig: SearchConfig = {
    placeholder: 'Search entities by name...',
    properties: ['name']
  };

  sortOptions: SortOption[] = [
    { label: 'Name (A-Z)', property: 'name', direction: 'asc' },
    { label: 'Name (Z-A)', property: 'name', direction: 'desc' }
  ];

  entitySearchTerm = '';
  entitySortOption: SortOption | null = null;
  visibleEntities: Entity[] = [];
  showEntityFilters = false;

  showAddRelationModal = false;
  showRelationsPreviewModal = false;
  showEntityRelationsPreviewModal = false;
  selectedEntityRelations: Relation[] = [];
  selectedEntityNameForRelations = '';
  editingRelation: Relation | null = null;
  editingRelationIndex: number | null = null;
  deletingRelationIndex: number | null = null;
  viewingEntityIndex: number | null = null;
  viewingEntitySource: 'main' | 'import' = 'main';
  showFieldDeleteConfirmation = false;
  fieldDeleteIndex: number | null = null;
  showImportEntityDeleteConfirmation = false;
  importDeleteIndex: number | null = null;

  fieldDeleteModalConfig = {
    title: 'Delete Field',
    message: [''],
    buttons: [
      { text: 'Cancel', type: 'cancel' as const, action: 'cancel' as const },
      { text: 'Delete', type: 'danger' as const, action: 'confirm' as const }
    ]
  };

  importEntityDeleteModalConfig = {
    title: 'Delete Entity',
    message: [''],
    buttons: [
      { text: 'Cancel', type: 'cancel' as const, action: 'cancel' as const },
      { text: 'Delete', type: 'danger' as const, action: 'confirm' as const }
    ]
  };

  deleteModalConfig = {
    title: 'Delete Entity',
    message: [''],
    buttons: [
      { text: 'Cancel', type: 'cancel' as const, action: 'cancel' as const },
      { text: 'Delete', type: 'danger' as const, action: 'confirm' as const }
    ]
  };

  constructor(
    private ddlImportService: DdlImportService,
    private toastService: ToastService,
    private router: Router
  ) {}

  get enumTypeNames(): string[] {
    return (this.enums ?? []).map(item => String(item?.name ?? '').trim()).filter(Boolean);
  }

  openInProgress(event: Event): void {
    event.preventDefault();
    this.router.navigate(['/in-progress']);
  }

  ngOnInit(): void {
    this.updateVisibleEntities();
  }

  addEntity(): void {
    this.editingEntity = null;
    this.editingEntityIndex = null;
    this.showAddEntityModal = true;
  }

  editEntity(entity: Entity, index: number): void {
    this.editingEntity = JSON.parse(JSON.stringify(entity));
    this.editingEntityIndex = index;
    this.showAddEntityModal = true;
  }

  deleteEntity(index: number): void {
    this.deletingEntityIndex = index;
    this.deletingEntityName = this.entities[index].name;
    this.deleteModalConfig.message = [
      `Are you sure you want to delete the entity "${this.deletingEntityName}"?`,
      'This action cannot be undone and all associated fields will be removed.'
    ];
    this.showDeleteConfirmation = true;
  }

  confirmDelete(): void {
    const removingEntity = this.deletingEntityIndex !== null;
    if (this.deletingEntityIndex !== null) {
      this.entities.splice(this.deletingEntityIndex, 1);
    } else if (this.deletingRelationIndex !== null) {
      this.relations.splice(this.deletingRelationIndex, 1);
    }
    if (removingEntity && this.entities.length === 0) {
      this.showEntityFilters = false;
      this.resetEntitySearch();
    }
    this.updateVisibleEntities();
    this.cancelDelete();
  }

  cancelDelete(): void {
    this.showDeleteConfirmation = false;
    this.deletingEntityIndex = null;
    this.deletingEntityName = '';
    this.deletingRelationIndex = null;
  }

  onEntitySave(entity: Entity): void {
    if (this.editingEntityIndex !== null) {
      this.entities[this.editingEntityIndex] = entity;
    } else {
      this.entities.push(entity);
    }
    this.entitiesChange.emit(JSON.parse(JSON.stringify(this.entities)));
    this.updateVisibleEntities();
    this.showAddEntityModal = false;
    this.editingEntity = null;
    this.editingEntityIndex = null;
  }

  onEntityCancel(): void {
    this.showAddEntityModal = false;
    this.editingEntity = null;
    this.editingEntityIndex = null;
  }

  closeModal(): void {
    this.showAddEntityModal = false;
    this.editingEntity = null;
    this.editingEntityIndex = null;
  }

  saveEntity(): void {
    if (this.addEntityComponent) {
      this.addEntityComponent.onSave();
    }
  }

  toggleEntitiesPanel(): void {
    this.entitiesExpanded = !this.entitiesExpanded;
  }

  toggleRelationsPanel(): void {
    this.relationsExpanded = !this.relationsExpanded;
  }

  addRelation(): void {
    if (this.entities.length === 0) {
      this.toastService.error('Add at least one entity before creating a relation.');
      return;
    }
    this.editingRelation = null;
    this.editingRelationIndex = null;
    this.showAddRelationModal = true;
  }

  openRelationsPreview(): void {
    if (this.relations.length === 0) {
      this.toastService.error('No relations available.');
      return;
    }
    this.showRelationsPreviewModal = true;
  }

  closeRelationsPreview(): void {
    this.showRelationsPreviewModal = false;
  }

  openEntityRelationsPreview(event: { entity: Entity; index: number }): void {
    const entityName = event.entity?.name;
    if (!entityName) {
      return;
    }
    const related = this.relations.filter(
      relation => relation.sourceEntity === entityName || relation.targetEntity === entityName
    );
    if (related.length === 0) {
      this.toastService.error('No relations available for this entity.');
      return;
    }
    this.selectedEntityRelations = related;
    this.selectedEntityNameForRelations = entityName;
    this.showEntityRelationsPreviewModal = true;
  }

  closeEntityRelationsPreview(): void {
    this.showEntityRelationsPreviewModal = false;
    this.selectedEntityRelations = [];
    this.selectedEntityNameForRelations = '';
  }

  editRelation(relation: Relation, index: number): void {
    this.editingRelation = JSON.parse(JSON.stringify(relation));
    this.editingRelationIndex = index;
    this.showAddRelationModal = true;
  }

  deleteRelation(index: number): void {
    this.deletingRelationIndex = index;
    this.deleteModalConfig.title = 'Delete Relation';
    this.deleteModalConfig.message = [
      `Are you sure you want to delete this relation?`,
      'This action cannot be undone.'
    ];
    this.showDeleteConfirmation = true;
  }

  onRelationSave(relation: Relation): void {
    if (this.editingRelationIndex !== null) {
      this.relations[this.editingRelationIndex] = relation;
    } else {
      this.relations.push(relation);
    }
    this.showAddRelationModal = false;
    this.editingRelation = null;
    this.editingRelationIndex = null;
  }

  onRelationCancel(): void {
    this.showAddRelationModal = false;
    this.editingRelation = null;
    this.editingRelationIndex = null;
  }

  closeRelationModal(): void {
    this.showAddRelationModal = false;
    this.editingRelation = null;
    this.editingRelationIndex = null;
  }

  saveRelation(): void {
    if (this.addRelationComponent) {
      this.addRelationComponent.onSave();
    }
  }

  getVisibleFields(entity: Entity): any[] {
    if (!entity.fields) return [];
    return entity.fields.slice(0, 5);
  }

  hasMoreFields(entity: Entity): boolean {
    return entity.fields && entity.fields.length > 5;
  }

  showEntityDetail(entity: Entity): void {
    this.viewingEntity = entity;
    this.showEntityDetailModal = true;
  }

  closeEntityDetail(): void {
    this.showEntityDetailModal = false;
    this.viewingEntity = null;
    this.viewingEntityIndex = null;
    if (this.viewingEntitySource === 'import') {
      this.showImportSchemaPreviewModal = true;
    }
  }

  openEntityDetailFromMain(event: { entity: Entity; index: number }): void {
    this.viewingEntitySource = 'main';
    const index = this.getEntityIndex(event.entity);
    if (index !== null) {
      this.viewingEntityIndex = index;
      this.showEntityDetail(this.entities[index]);
    }
  }

  openEntityDetailFromImport(event: { entity: DdlEntity; index: number }): void {
    this.viewingEntitySource = 'import';
    this.viewingEntityIndex = event.index;
    this.showImportSchemaPreviewModal = false;
    this.showEntityDetail(event.entity as Entity);
  }

  editImportEntity(): void {
    this.showImportSchemaPreviewModal = false;
    this.showImportSchemaModal = true;
  }

  requestImportEntityDelete(event: { entity: DdlEntity; index: number }): void {
    const name = event.entity?.name ?? 'this entity';
    this.importDeleteIndex = event.index;
    this.importEntityDeleteModalConfig.message = [
      `Are you sure you want to delete "${name}" from the import preview?`,
      'This will remove it from the import list.'
    ];
    this.showImportEntityDeleteConfirmation = true;
  }

  confirmImportEntityDelete(): void {
    if (this.importDeleteIndex === null) {
      this.cancelImportEntityDelete();
      return;
    }

    this.importPreviewEntities.splice(this.importDeleteIndex, 1);
    this.cancelImportEntityDelete();
  }

  cancelImportEntityDelete(): void {
    this.showImportEntityDeleteConfirmation = false;
    this.importDeleteIndex = null;
  }

  deleteEntityField(fieldIndex: number): void {
    if (this.viewingEntityIndex === null) {
      return;
    }

    if (this.viewingEntitySource === 'main') {
      const target = this.entities[this.viewingEntityIndex];
      const fieldName = target?.fields?.[fieldIndex]?.name ?? 'this field';
      this.fieldDeleteIndex = fieldIndex;
      this.fieldDeleteModalConfig.message = [
        `Are you sure you want to delete "${fieldName}"?`,
        'This action cannot be undone.'
      ];
      this.showFieldDeleteConfirmation = true;
    }
  }

  confirmDeleteField(): void {
    if (this.viewingEntityIndex === null || this.fieldDeleteIndex === null) {
      this.cancelDeleteField();
      return;
    }

    const target = this.entities[this.viewingEntityIndex];
    if (target?.fields) {
      target.fields.splice(this.fieldDeleteIndex, 1);
    }
    this.cancelDeleteField();
  }

  cancelDeleteField(): void {
    this.showFieldDeleteConfirmation = false;
    this.fieldDeleteIndex = null;
  }

  openImportSchema(event: MouseEvent): void {
    event.preventDefault();
    this.showImportSchemaModal = true;
  }

  triggerSchemaUpload(fileInput: HTMLInputElement, event: MouseEvent): void {
    event.preventDefault();
    fileInput.click();
  }

  onSchemaFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];

    if (!file) {
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      const content = typeof reader.result === 'string' ? reader.result : '';
      if (!content.trim()) {
        this.toastService.error('Invalid SQL data.');
        input.value = '';
        return;
      }

      this.importSchemaSql = content;
      this.openSchemaPreviewFromSql(content);
      input.value = '';
    };
    reader.onerror = () => {
      this.toastService.error('Failed to read uploaded SQL file.');
      input.value = '';
    };

    reader.readAsText(file);
  }

  closeImportSchema(): void {
    this.showImportSchemaModal = false;
    this.importSchemaSql = '';
  }

  goToSchemaPreview(): void {
    this.openSchemaPreviewFromSql(this.importSchemaSql);
  }

  goBackToImportSchema(): void {
    this.showImportSchemaPreviewModal = false;
    this.showImportSchemaModal = true;
  }

  closeImportSchemaPreview(): void {
    this.showImportSchemaPreviewModal = false;
    this.importSchemaSql = '';
  }

  performSchemaImport(): void {
    const mappedEntities = this.importPreviewEntities.map(entity => ({
      name: entity.name,
      mappedSuperclass: false,
      addRestEndpoints: this.importAddRestEndpoints,
      auditable: false,
      softDelete: false,
      immutable: false,
      naturalIdCache: false,
      fields: entity.fields.map(field => ({
        name: field.name,
        type: field.type,
        maxLength: field.maxLength,
        primaryKey: Boolean(field.primaryKey),
        required: Boolean(field.required),
        unique: false
      }))
    }));

    const existingByName = new Map(this.entities.map(entity => [entity.name.toLowerCase(), entity]));
    for (const imported of mappedEntities) {
      const key = imported.name.toLowerCase();
      if (existingByName.has(key)) {
        const index = this.entities.findIndex(entity => entity.name.toLowerCase() === key);
        if (index >= 0) {
          this.entities[index] = this.mergeEntityFields(this.entities[index], imported);
        }
      } else {
        this.entities.push(imported);
      }
    }

    this.toastService.success(`Imported ${mappedEntities.length} entities from SQL.`);
    this.showImportSchemaPreviewModal = false;
    this.importSchemaSql = '';
    this.importPreviewEntities = [];
    this.importAddRestEndpoints = false;
    this.updateVisibleEntities();
  }

  private openSchemaPreviewFromSql(sqlScript: string): void {
    try {
      const parsed = this.ddlImportService.parse(sqlScript);
      if (parsed.length === 0) {
        this.toastService.error('Invalid SQL data.');
        return;
      }

      this.importPreviewEntities = parsed;
      this.showImportSchemaModal = false;
      this.showImportSchemaPreviewModal = true;
    } catch (error: unknown) {
      const message = error instanceof Error && error.message
        ? error.message
        : 'Invalid SQL data.';
      this.toastService.error(message);
    }
  }

  private mergeEntityFields(existing: Entity, incoming: Entity): Entity {
    const mergedFields = [...(existing.fields ?? [])];
    const existingByName = new Map(
      mergedFields.map(field => [field.name.toLowerCase(), field])
    );

    for (const incomingField of incoming.fields ?? []) {
      const key = incomingField.name.toLowerCase();
      if (existingByName.has(key)) {
        const target = existingByName.get(key);
        if (target) {
          target.type = incomingField.type;
          target.maxLength = incomingField.maxLength;
          target.primaryKey = incomingField.primaryKey;
          target.required = incomingField.required;
        }
      } else {
        mergedFields.push(incomingField);
      }
    }

    return {
      ...existing,
      addRestEndpoints: incoming.addRestEndpoints,
      addCrudOperations: incoming.addCrudOperations ?? existing.addCrudOperations ?? false,
      auditable: existing.auditable ?? false,
      softDelete: existing.softDelete ?? false,
      immutable: existing.immutable ?? false,
      naturalIdCache: existing.naturalIdCache ?? false,
      fields: mergedFields
    };
  }

  onEntitySearchSortChange(event: SearchSortEvent): void {
    this.entitySearchTerm = event.searchTerm;
    this.entitySortOption = event.sortOption;
    this.updateVisibleEntities();
  }

  toggleEntityFilters(): void {
    if (this.entities.length === 0) {
      return;
    }
    this.showEntityFilters = !this.showEntityFilters;
    if (!this.showEntityFilters) {
      this.resetEntitySearch();
      this.updateVisibleEntities();
    }
  }

  editEntityFromList(event: { entity: Entity; index: number }): void {
    const index = this.getEntityIndex(event.entity);
    if (index !== null) {
      this.editEntity(this.entities[index], index);
    }
  }

  deleteEntityFromList(event: { entity: Entity; index: number }): void {
    const index = this.getEntityIndex(event.entity);
    if (index !== null) {
      this.deleteEntity(index);
    }
  }

  private updateVisibleEntities(): void {
    const normalizedSearch = this.entitySearchTerm.trim().toLowerCase();
    let results = [...this.entities];

    if (normalizedSearch) {
      results = results.filter(entity =>
        entity.name?.toLowerCase().includes(normalizedSearch)
      );
    }

    if (this.entitySortOption) {
      const direction = this.entitySortOption.direction;
      results = results.sort((a, b) => {
        const aValue = a.name?.toLowerCase() ?? '';
        const bValue = b.name?.toLowerCase() ?? '';
        const comparison = aValue < bValue ? -1 : aValue > bValue ? 1 : 0;
        return direction === 'asc' ? comparison : -comparison;
      });
    }

    this.visibleEntities = results;
  }

  private resetEntitySearch(): void {
    this.entitySearchTerm = '';
    if (this.entitySearchSortComponent) {
      this.entitySearchSortComponent.clearSearch();
    }
  }

  private getEntityIndex(entity: Entity): number | null {
    const name = entity?.name?.toLowerCase();
    if (!name) {
      return null;
    }
    const index = this.entities.findIndex(item => item.name?.toLowerCase() === name);
    return index >= 0 ? index : null;
  }
}
