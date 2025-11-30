import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AddEntityComponent } from '../add-entity/add-entity.component';

interface Entity {
  name: string;
  mappedSuperclass: boolean;
  addRestEndpoints: boolean;
  fields: any[];
}

interface Relation {
  sourceEntity: string;
  targetEntity: string;
  relationType: string;
  fieldName: string;
}

@Component({
  selector: 'app-entities',
  standalone: true,
  imports: [CommonModule, AddEntityComponent],
  templateUrl: './entities.component.html',
  styleUrls: ['./entities.component.css']
})
export class EntitiesComponent {
  @Input() entities: Entity[] = [];
  @Input() relations: Relation[] = [];

  showAddEntity = false;
  entitiesExpanded = true;
  relationsExpanded = true;
  editingEntity?: Entity;

  addEntity(): void {
    this.editingEntity = undefined;
    this.showAddEntity = true;
  }

  editEntity(entity: Entity): void {
    this.editingEntity = entity;
    this.showAddEntity = true;
  }

  deleteEntity(entity: Entity): void {
    const index = this.entities.indexOf(entity);
    if (index > -1) {
      this.entities.splice(index, 1);
    }
  }

  onEntitySave(entity: Entity): void {
    if (this.editingEntity) {
      const index = this.entities.indexOf(this.editingEntity);
      if (index > -1) {
        this.entities[index] = entity;
      }
    } else {
      this.entities.push(entity);
    }
    this.showAddEntity = false;
    this.editingEntity = undefined;
  }

  onEntityCancel(): void {
    this.showAddEntity = false;
    this.editingEntity = undefined;
  }

  toggleEntitiesPanel(): void {
    this.entitiesExpanded = !this.entitiesExpanded;
  }

  toggleRelationsPanel(): void {
    this.relationsExpanded = !this.relationsExpanded;
  }

  addRelation(): void {
    console.log('Add relation clicked');
  }
}
