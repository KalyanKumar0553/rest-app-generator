import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

interface Field {
  type: string;
  name: string;
  maxLength?: number;
  primaryKey?: boolean;
  required?: boolean;
  unique?: boolean;
}

interface Entity {
  name: string;
  mappedSuperclass?: boolean;
  addRestEndpoints?: boolean;
  fields: Field[];
}

@Component({
  selector: 'app-entity-detail-view',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './entity-detail-view.component.html',
  styleUrls: ['./entity-detail-view.component.css']
})
export class EntityDetailViewComponent {
  @Input() entity: Entity | null = null;
  @Input() canDeleteFields = true;
  @Output() close = new EventEmitter<void>();
  @Output() deleteField = new EventEmitter<number>();

  onClose(): void {
    this.close.emit();
  }

  onDeleteField(index: number): void {
    if (!this.canDeleteFields) {
      return;
    }
    this.deleteField.emit(index);
  }
}
