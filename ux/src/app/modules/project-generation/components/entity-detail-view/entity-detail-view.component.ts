import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HelpPopoverComponent } from '../../../../components/help-popover/help-popover.component';

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
  addCrudOperations?: boolean;
  fields: Field[];
}

@Component({
  selector: 'app-entity-detail-view',
  standalone: true,
  imports: [CommonModule, HelpPopoverComponent],
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

  getFieldConstraintCount(field: Field): number {
    let count = 0;
    if (field.primaryKey) {
      count += 1;
    }
    if (field.required) {
      count += 1;
    }
    if (field.unique) {
      count += 1;
    }
    return count;
  }
}
