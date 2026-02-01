import { Component, Input, Output, EventEmitter, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from '../../../../components/modal/modal.component';
import { AddEntityComponent } from '../add-entity/add-entity.component';
import { AddRelationComponent, Relation } from '../add-relation/add-relation.component';
import { ConfirmationModalComponent } from '../../../../components/confirmation-modal/confirmation-modal.component';
import { EntityDetailViewComponent } from '../entity-detail-view/entity-detail-view.component';
import { ImportSchemaComponent } from '../import-schema/import-schema.component';
import { ImportSchemaPreviewComponent } from '../import-schema-preview/import-schema-preview.component';
import { PreviewEntitiesComponent } from '../../../../components/preview-entities/preview-entities.component';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { Field } from '../field-item/field-item.component';
import { DdlEntity, DdlImportService } from '../../../../services/ddl-import.service';
import { ToastService } from '../../../../services/toast.service';

interface Entity {
  name: string;
  mappedSuperclass?: boolean;
  addRestEndpoints?: boolean;
  fields?: Field[];
}


@Component({
  selector: 'app-entities',
  standalone: true,
  imports: [CommonModule, ModalComponent, AddEntityComponent, AddRelationComponent, ConfirmationModalComponent, EntityDetailViewComponent, ImportSchemaComponent, ImportSchemaPreviewComponent, PreviewEntitiesComponent, MatIconModule, MatButtonModule],
  templateUrl: './entities.component.html',
  styleUrls: ['./entities.component.css']
})
export class EntitiesComponent {
  @Input() entities: Entity[] = [];
  @Input() relations: Relation[] = [];
  @Output() entitiesChange = new EventEmitter<Entity[]>();
  @ViewChild(AddEntityComponent) addEntityComponent!: AddEntityComponent;
  @ViewChild(AddRelationComponent) addRelationComponent!: AddRelationComponent;

  showInfoBanner = true;

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

  showAddRelationModal = false;
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
    private toastService: ToastService
  ) {}

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
    if (this.deletingEntityIndex !== null) {
      this.entities.splice(this.deletingEntityIndex, 1);
    } else if (this.deletingRelationIndex !== null) {
      this.relations.splice(this.deletingRelationIndex, 1);
    }
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

  closeInfoBanner(): void {
    this.showInfoBanner = false;
  }

  toggleEntitiesPanel(): void {
    this.entitiesExpanded = !this.entitiesExpanded;
  }

  toggleRelationsPanel(): void {
    this.relationsExpanded = !this.relationsExpanded;
  }

  addRelation(): void {
    this.editingRelation = null;
    this.editingRelationIndex = null;
    this.showAddRelationModal = true;
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
    this.viewingEntityIndex = event.index;
    this.showEntityDetail(event.entity);
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

  closeImportSchema(): void {
    this.showImportSchemaModal = false;
    this.importSchemaSql = '';
  }

  goToSchemaPreview(): void {
    const parsed = this.ddlImportService.parse(this.importSchemaSql);
    if (parsed.length === 0) {
      this.toastService.error('No tables found in the SQL script.');
      return;
    }

    this.importPreviewEntities = parsed;
    this.showImportSchemaModal = false;
    this.showImportSchemaPreviewModal = true;
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
      fields: mergedFields
    };
  }
}
