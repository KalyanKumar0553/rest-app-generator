import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from '../../../../components/modal/modal.component';
import { AddEntityComponent } from '../add-entity/add-entity.component';

interface Entity {
  name: string;
  fields?: any[];
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
  imports: [CommonModule, ModalComponent, AddEntityComponent],
  templateUrl: './entities.component.html',
  styleUrls: ['./entities.component.css']
})
export class EntitiesComponent {
  @Input() entities: Entity[] = [];
  @Input() relations: Relation[] = [];

  entitiesExpanded = true;
  relationsExpanded = true;
  showAddEntityModal = false;

  addEntity(): void {
    this.showAddEntityModal = true;
  }

  onEntitySave(entity: Entity): void {
    this.entities.push(entity);
    this.showAddEntityModal = false;
  }

  onEntityCancel(): void {
    this.showAddEntityModal = false;
  }

  closeModal(): void {
    this.showAddEntityModal = false;
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
