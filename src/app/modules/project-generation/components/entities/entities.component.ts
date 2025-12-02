import { Component, Input, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from '../../../../components/modal/modal.component';
import { AddEntityComponent } from '../add-entity/add-entity.component';
import { AddRelationComponent, Relation } from '../add-relation/add-relation.component';
import { ConfirmationModalComponent, ModalButton } from '../../../../components/confirmation-modal/confirmation-modal.component';
import { EntityDetailViewComponent } from '../entity-detail-view/entity-detail-view.component';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

interface Entity {
  name: string;
  fields?: any[];
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
}
