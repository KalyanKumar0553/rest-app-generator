import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { EntitiesComponent } from '../../entities/entities.component';

@Component({
  selector: 'app-node-entities',
  standalone: true,
  imports: [CommonModule, EntitiesComponent],
  template: `
    <app-entities
      [entities]="entities"
      [relations]="relations"
      [enums]="enums"
      [dataObjects]="dataObjects"
      (entitiesChange)="entitiesChange.emit($event)">
    </app-entities>
  `
})
export class NodeEntitiesComponent {
  @Input() entities: any[] = [];
  @Input() relations: any[] = [];
  @Input() enums: any[] = [];
  @Input() dataObjects: any[] = [];
  @Output() entitiesChange = new EventEmitter<any[]>();
}
