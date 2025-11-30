import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

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
  imports: [CommonModule],
  templateUrl: './entities.component.html',
  styleUrls: ['./entities.component.css']
})
export class EntitiesComponent {
  @Input() entities: Entity[] = [];
  @Input() relations: Relation[] = [];

  entitiesExpanded = true;
  relationsExpanded = true;

  addEntity(): void {
    console.log('Add entity clicked');
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
