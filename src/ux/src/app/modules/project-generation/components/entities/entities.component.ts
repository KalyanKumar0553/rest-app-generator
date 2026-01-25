import { Component, Input, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from '../../../../components/modal/modal.component';
import { AddEntityComponent } from '../add-entity/add-entity.component';
import { AddRelationComponent, Relation } from '../add-relation/add-relation.component';
import { ConfirmationModalComponent, ModalButton } from '../../../../components/confirmation-modal/confirmation-modal.component';
import { EntityDetailViewComponent } from '../entity-detail-view/entity-detail-view.component';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { ToastService } from '../../../../services/toast.service';
import { Field } from '../field-item/field-item.component';

interface Entity {
  name: string;
  mappedSuperclass?: boolean;
  addRestEndpoints?: boolean;
  fields?: Field[];
}


@Component({
  selector: 'app-entities',
  standalone: true,
  imports: [CommonModule, ModalComponent, AddEntityComponent, AddRelationComponent, ConfirmationModalComponent, EntityDetailViewComponent, MatIconModule, MatButtonModule],
  templateUrl: './entities.component.html',
  styleUrls: ['./entities.component.css']
})
export class EntitiesComponent {
  @Input() entities: Entity[] = [];
  @Input() relations: Relation[] = [];
  @ViewChild(AddEntityComponent) addEntityComponent!: AddEntityComponent;
  @ViewChild(AddRelationComponent) addRelationComponent!: AddRelationComponent;
  @ViewChild('schemaFileInput') schemaFileInput?: ElementRef<HTMLInputElement>;

  showInfoBanner = true;

  entitiesExpanded = true;
  relationsExpanded = true;
  showAddEntityModal = false;
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

  deleteModalConfig = {
    title: 'Delete Entity',
    message: [''],
    buttons: [
      { text: 'Cancel', type: 'cancel' as const, action: 'cancel' as const },
      { text: 'Delete', type: 'danger' as const, action: 'confirm' as const }
    ]
  };

  constructor(private toastService: ToastService) {}

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
  }

  triggerSchemaImport(event: MouseEvent): void {
    event.preventDefault();
    this.schemaFileInput?.nativeElement.click();
  }

  onSchemaFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files && input.files.length > 0 ? input.files[0] : null;
    if (!file) {
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      try {
        const raw = typeof reader.result === 'string' ? reader.result : '';
        const parsed = JSON.parse(raw);
        const { entities, relations } = this.normalizeSchema(parsed);

        if (entities.length === 0) {
          this.toastService.error('No entities found in the schema file.');
          return;
        }

        this.replaceSchema(entities, relations);
        this.toastService.success(`Imported ${entities.length} entities and ${relations.length} relations.`);
      } catch (error) {
        console.error('Schema import failed:', error);
        this.toastService.error('Invalid schema file. Please provide valid JSON.');
      } finally {
        input.value = '';
      }
    };
    reader.onerror = () => {
      this.toastService.error('Failed to read schema file.');
      input.value = '';
    };
    reader.readAsText(file);
  }

  private normalizeSchema(data: any): { entities: Entity[]; relations: Relation[] } {
    const rawEntities = this.extractEntities(data);
    const entities = rawEntities
      .map(entity => this.normalizeEntity(entity))
      .filter((entity): entity is Entity => entity !== null);

    const entityNames = new Set(entities.map(entity => entity.name));
    const rawRelations = this.extractRelations(data);
    const relations = rawRelations
      .map(relation => this.normalizeRelation(relation))
      .filter((relation): relation is Relation => relation !== null)
      .filter(relation => entityNames.has(relation.sourceEntity) && entityNames.has(relation.targetEntity));

    return { entities, relations };
  }

  private extractEntities(data: any): any[] {
    if (Array.isArray(data)) {
      return data;
    }
    if (Array.isArray(data?.entities)) {
      return data.entities;
    }
    if (Array.isArray(data?.schema?.entities)) {
      return data.schema.entities;
    }
    if (Array.isArray(data?.model?.entities)) {
      return data.model.entities;
    }
    return [];
  }

  private extractRelations(data: any): any[] {
    if (Array.isArray(data?.relations)) {
      return data.relations;
    }
    if (Array.isArray(data?.schema?.relations)) {
      return data.schema.relations;
    }
    if (Array.isArray(data?.model?.relations)) {
      return data.model.relations;
    }
    if (Array.isArray(data?.relationships)) {
      return data.relationships;
    }
    return [];
  }

  private normalizeEntity(raw: any): Entity | null {
    const name = this.normalizeString(
      raw?.name ?? raw?.entityName ?? raw?.table ?? raw?.tableName ?? raw?.entity
    );
    if (!name) {
      return null;
    }

    const rawFields = Array.isArray(raw?.fields)
      ? raw.fields
      : Array.isArray(raw?.columns)
        ? raw.columns
        : Array.isArray(raw?.attributes)
          ? raw.attributes
          : [];

    const fields = rawFields
      .map(field => this.normalizeField(field))
      .filter((field): field is Field => field !== null);

    if (fields.length === 0) {
      fields.push({
        type: 'Long',
        name: 'id',
        primaryKey: true,
        required: false,
        unique: false
      });
    }

    return {
      name,
      mappedSuperclass: Boolean(raw?.mappedSuperclass),
      addRestEndpoints: Boolean(raw?.addRestEndpoints),
      fields
    };
  }

  private normalizeField(raw: any): Field | null {
    const name = this.normalizeString(
      raw?.name ?? raw?.fieldName ?? raw?.column ?? raw?.columnName ?? raw?.attribute
    );
    const type = this.normalizeString(raw?.type ?? raw?.fieldType ?? raw?.dataType) || 'String';

    if (!name) {
      return null;
    }

    const maxLength = this.normalizeNumber(raw?.maxLength ?? raw?.length ?? raw?.size);

    return {
      name,
      type,
      maxLength: maxLength ?? undefined,
      primaryKey: Boolean(raw?.primaryKey ?? raw?.id ?? raw?.isId),
      required: Boolean(raw?.required ?? raw?.notNull ?? raw?.nullable === false),
      unique: Boolean(raw?.unique ?? raw?.isUnique)
    };
  }

  private normalizeRelation(raw: any): Relation | null {
    const sourceEntity = this.normalizeString(
      raw?.sourceEntity ?? raw?.source ?? raw?.from ?? raw?.ownerEntity
    );
    const targetEntity = this.normalizeString(
      raw?.targetEntity ?? raw?.target ?? raw?.to ?? raw?.inverseEntity
    );
    const relationType = this.normalizeRelationType(raw?.relationType ?? raw?.type ?? raw?.cardinality);
    const sourceFieldName = this.normalizeString(
      raw?.sourceFieldName ?? raw?.sourceField ?? raw?.fieldName ?? raw?.name
    );
    const targetFieldName = this.normalizeString(raw?.targetFieldName ?? raw?.targetField ?? raw?.inverseField);

    if (!sourceEntity || !targetEntity || !relationType || !sourceFieldName) {
      return null;
    }

    return {
      sourceEntity,
      sourceFieldName,
      targetEntity,
      targetFieldName: targetFieldName || undefined,
      relationType,
      required: Boolean(raw?.required ?? raw?.notNull ?? raw?.mandatory)
    };
  }

  private normalizeRelationType(value: any): string {
    const normalized = this.normalizeString(value);
    if (!normalized) {
      return '';
    }
    const compact = normalized.toLowerCase().replace(/[^a-z0-9]/g, '');
    if (compact === 'onetoone') {
      return 'OneToOne';
    }
    if (compact === 'onetomany') {
      return 'OneToMany';
    }
    if (compact === 'manytoone') {
      return 'ManyToOne';
    }
    if (compact === 'manytomany') {
      return 'ManyToMany';
    }
    return normalized;
  }

  private normalizeString(value: any): string {
    if (value === null || value === undefined) {
      return '';
    }
    return String(value).trim();
  }

  private normalizeNumber(value: any): number | null {
    if (value === null || value === undefined || value === '') {
      return null;
    }
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : null;
  }

  private replaceSchema(entities: Entity[], relations: Relation[]): void {
    this.entities.splice(0, this.entities.length, ...entities);
    this.relations.splice(0, this.relations.length, ...relations);
  }
}
